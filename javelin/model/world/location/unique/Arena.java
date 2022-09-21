package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.challenge.Tier;
import javelin.controller.comparator.CombatantByCr;
import javelin.controller.comparator.CombatantsByNameAndMercenary;
import javelin.controller.comparator.MonstersByCr;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.LocationFight;
import javelin.controller.content.fight.mutator.Friendly;
import javelin.controller.content.kit.Kit;
import javelin.controller.content.map.location.LocationMap;
import javelin.controller.db.EncounterIndex;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.unit.Squad;
import javelin.model.world.Incursion;
import javelin.model.world.Period;
import javelin.model.world.Period.Time;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;

/**
 * The Arena is a sort of {@link Minigame} that has an excuse to scale with
 * {@link Squad} level but it also serves the purpose of being a relatively safe
 * leveling venue early on when wandering the game {@link World} might be too
 * risky. Arena fights are {@link Difficulty#DIFFICULT} by design to make them
 * more of a spectacle, with the drawback that losing a fight yiels no rewards -
 * characters can die but being {@link Fight#friendly} makes it less likely.
 *
 * One issue with the Arean is that it's easy to fall into a rhythm of endless
 * grinding, sending your hurt {@link Combatant}s to rest or train and continue
 * fighting for experience. While that approach is sub-optimal for several other
 * reaons (better rewards in {@link DungeonFloor}s, letting {@link Incursion}s
 * run rampant will make your life harder in the long run, less difficult
 * encounters in level-appropriate areas meaning better chances of not losing a
 * {@link Squad} member and not losing and getting away with nothing)... but
 * despite this, being designed as a safe-zone, the allure of not doing anything
 * else is seductive. The current approach to prevent this is to introduce
 * "business hours" but if that's not enough, it might be necessary to have
 * events only on certain days and have a UI indicator for that as well.
 *
 * Another anti-grinding incentive is that the {@link ArenaFight}'s EL is capped
 * by the starting {@link Town}'s {@link Rank#maxpopulation}. This soft-prevents
 * players from grinding to level 20 by simply playing Arena endlessly and also
 * thematically such silly scenarios as waves of epic level 20 heroes coming to
 * participate in tournaments in a lowly {@link Rank#HAMLET}.
 *
 * TODO should be considered a winnable game objective?
 *
 * TODO allow escape at any point (even if engaged) - just moving all units to
 * BattleState#fleeing should suffice?
 *
 * TODO the "business hours" mechanic would be great for all {@link District}
 * buildings as a simulationist mechanic - however, it is better left for after
 * 2.0 when hopefully there's a bisual indication of the time of day in the UI.
 *
 * TODO another interesting battle mode would be a Champion fight, where 1d8
 * unique NPC units are generated - making it more like a MOBA teamfight (no
 * allies for those)?
 *
 * TODO last balance test: level 10 in 300 days
 *
 * @author alex
 */
public class Arena extends UniqueLocation{
  static final String NONEELIGIBLE="Only gladiators in good health are allowed to fight in the arena!";
  static final String CONFIRM="Begin an Arena match?\n"
      +"Press ENTER to confirm or any other key to cancel...\n\n";
  static final String DESCRIPTION="The Arena";
  static final EncounterIndex GLADIATORS=new EncounterIndex(Monster.ALL.stream()
      .filter(m->m.think(-1)&&MonsterType.HUMANOID.equals(m.type))
      .collect(Collectors.toList()));

  class ArenaMap extends LocationMap{
    List<Point> minionspawn=new ArrayList<>();

    public ArenaMap(){
      super("colosseum");
      wall=Images.get(List.of("terrain","orcwall"));
      floor=Images.get(List.of("terrain","desert"));
    }

    @Override
    protected Square processtile(Square s,int x,int y,char c){
      if(c=='3') minionspawn.add(new Point(x,y));
      return super.processtile(s,x,y,c);
    }
  }

  class ArenaFight extends LocationFight{
    Team team;

    ArenaFight(Team t){
      super(Arena.this,new ArenaMap());
      team=t;
      mutators.add(new Friendly(Combatant.STATUSINJURED));
      period=Period.AFTERNOON;
      goldbonus=.5;
      canflee=false;
    }

    @Override
    public ArrayList<Combatant> getfoes(Integer teamel){
      return team;
    }

    @Override
    public boolean onend(){
      var s=Fight.state;
      var red=s.getcombatants();
      red.addAll(s.dead);
      red.retainAll(team);
      for(var t:new Combatants(team)){
        t.hp=red.get(red.indexOf(t)).hp;
        if(t.hp<=Combatant.DEADATHP) team.remove(t);
      }
      return super.onend();
    }

    @Override
    public String reward(){
      var r=super.reward();
      var t=(Town)findclosest(Town.class);
      var d=t.diplomacy;
      var from=d.describestatus();
      var chance=RewardCalculator.getgold(20)/(Fight.gold*t.population);
      if(chance<1) chance=1;
      if(Javelin.DEBUG)
        r+="(Reputation increase chance: 1/%s).\n".formatted(chance);
      if(RPG.chancein(chance)) d.reputation+=1;
      var to=d.describestatus();
      if(to!=from)
        r+="Reputation in %s increases to %s!\n".formatted(t,to.toLowerCase());
      return r;
    }
  }

  static class Team extends Combatants{
    Map<Combatant,Kit> kits=new HashMap<>();
    List<Monster> candidates;
    String type;

    Team(String type,List<Monster> candidates){
      this.type=type;
      this.candidates=new ArrayList<>(candidates);
      RPG.shuffle(this.candidates).sort(MonstersByCr.SINGLETON);
      while(size()<2) hire();
    }

    boolean hire(){
      var current=stream().map(c->c.source.name).toList();
      var candidates=this.candidates.stream()
          .filter(m->!current.contains(m.name)).toList();
      if(candidates.isEmpty()) return false;
      var c=new Combatant(RPG.select(candidates),true);
      add(c);
      var k=RPG.pick(Kit.getpreferred(c.source,getel()>=Tier.MID.minlevel));
      k.rename(c.source);
      kits.put(c,k);
      return true;
    }

    @Override
    public String toString(){
      return List.of(type,getel(),super.toString()).toString();
    }

    static List<Team> setup(){
      var teams=new ArrayList<Team>();
      var types=RPG.shuffle(
          new ArrayList<>(List.of(MonsterType.FEY,MonsterType.HUMANOID)));
      for(var t:types){
        var candidates=new ArrayList<>(
            Monster.get(t).stream().filter(m->m.think(-1)).toList());
        var subtypes=RPG.shuffle(new ArrayList<>(candidates.stream()
            .flatMap(m->m.subtypes.stream()).distinct().toList()));
        for(var s:subtypes){
          var subtyped=candidates.stream().filter(m->m.subtypes.contains(s))
              .toList();
          if(subtyped.size()<3) continue;
          candidates.removeAll(subtyped);
          teams.add(new Team(s,subtyped));
        }
        if(candidates.size()>=3)
          teams.add(new Team(t.toString().toLowerCase(),candidates));
      }
      return teams;
    }

    boolean ready(){
      if(isEmpty()) return false;
      return stream()
          .filter(c->c.getconditions().size()>0
              ||c.getnumericstatus()!=Combatant.STATUSUNHARMED)
          .findAny().isEmpty();
    }

    void rest(){
      System.out.println(
          stream().map(c->"%s (%s)".formatted(c,c.getstatus())).toList());
      for(var c:this) c.rest(2,24);
    }

    void upgrade(int el){
      if(getel()>=el) return;
      sort(CombatantByCr.SINGLETON);
      for(var c:this) while(kits.get(c).upgrade(c)){
        ChallengeCalculator.calculatecr(c.source);//TODO needed?
        if(getel()>=el) return;
      }
    }

    void turn(Arena a){
      rest();
      var upgrade=Period.Time.YEAR/20;
      if(RPG.chancein(size()==1?Time.WEEK:Time.SEASON)) hire();
      else if(RPG.chancein(upgrade)){
        var t=(Town)a.findclosest(Town.class);
        upgrade(t.population+4);
      }
    }
  }

  List<Team> teams=RPG.shuffle(Team.setup());

  /** Constructor. */
  public Arena(){
    super(DESCRIPTION,DESCRIPTION,15,20);
    generategarrison=false;
  }

  @Override
  public List<Combatant> getcombatants(){
    return null;
  }

  Combatant choosefighter(List<Combatant> squad,List<Combatant> fighters){
    if(squad.isEmpty()) return null;
    var prompt="Add which fighter to your team?\n\nCurrently selected: ";
    var current=fighters.isEmpty()?"none selected yet":Javelin.group(fighters);
    var choices=squad.stream().sorted(CombatantsByNameAndMercenary.SINGLETON)
        .map(c->c+" ("+c.getstatus()+")").collect(Collectors.toList());
    var choice=Javelin.choose(prompt+current+".",choices,true,false);
    return choice>=0?squad.get(choice):null;
  }

  @Override
  public boolean interact(){
    if(!super.interact()
        ||!isopen(List.of(Period.AFTERNOON,Period.EVENING),this))
      return false;
    var hurt=Squad.active.members.stream()
        .filter(c->c.getnumericstatus()<Combatant.STATUSSCRATCHED).limit(1);
    if(hurt.count()>0){
      Javelin.message(NONEELIGIBLE,false);
      return false;
    }
    var teams=this.teams.stream().filter(Team::ready)
        .sorted(Comparator.comparing(Team::getel).reversed()).toList();
    if(teams.isEmpty()){
      Javelin.message("All teams are currently in recovery...",true);
      return true;
    }
    if(Javelin.prompt(CONFIRM)=='\n')
      throw new StartBattle(new ArenaFight(RPG.select(teams)));
    return false;
  }

  @Override
  public void turn(long time,WorldScreen world){
    super.turn(time,world);
    for(var t:teams) t.turn(this);
  }
}
