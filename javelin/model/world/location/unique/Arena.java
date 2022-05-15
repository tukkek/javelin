package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.comparator.CombatantsByNameAndMercenary;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.LocationFight;
import javelin.controller.content.fight.mutator.Friendly;
import javelin.controller.content.fight.mutator.mode.Waves;
import javelin.controller.content.map.location.LocationMap;
import javelin.controller.db.EncounterIndex;
import javelin.controller.exception.GaveUp;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.unit.Squad;
import javelin.model.world.Incursion;
import javelin.model.world.Period;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.Images;

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
 * @author alex
 */
public class Arena extends UniqueLocation{
  static final String NONEELIGIBLE="Only gladiators in full health are allowed to fight in the arena!";
  static final String CONFIRM="Begin an Arena match with these fighters?\n"
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

  static class ArenaWaves extends Waves{
    Combatants allies;

    ArenaWaves(int elp,Combatants allies){
      super(elp);
      this.allies=allies;
      message="New gladiators enter the arena!";
    }

    @Override
    public void draw(Fight fight){
      var b=Fight.state.blueteam;
      fight.add(allies,b,((ArenaMap)fight.map).minionspawn);
      waveel=ChallengeCalculator.calculateel(b)+Waves.ELMODIFIER.get(waves);
      super.draw(fight);
    }

    @Override
    public Combatants generate(Fight f){
      return generate(waveel);
    }

    static Combatants generate(int targetel){
      try{
        var el=targetel-RPG.r(0,2);
        var e=EncounterGenerator.generate(el,GLADIATORS);
        return NpcGenerator.upgrade(e,targetel,true);
      }catch(GaveUp e){
        throw new RuntimeException(e);
      }
    }
  }

  class ArenaFight extends LocationFight{
    Combatants fighters=new Combatants();
    int alliesel=0;
    int teamel;
    int waveel;

    ArenaFight(ArrayList<Combatant> fighters,int el){
      super(Arena.this,new ArenaMap());
      mutators.add(new Friendly(Combatant.STATUSINJURED));
      this.fighters.addAll(fighters);
      period=Period.AFTERNOON;
      teamel=el;
      mutators.add(new ArenaWaves(el,generateallies()));
    }

    Combatants generateallies(){
      if(RPG.chancein(4)) return new Combatants(0);
      alliesel=ChallengeCalculator.calculateel(fighters)
          +RPG.r(Difficulty.EASY,0);
      var allies=ArenaWaves.generate(alliesel);
      for(var a:allies){
        a.automatic=true;
        a.mercenary=true;
      }
      return allies;
    }

    @Override
    public ArrayList<Combatant> getblueteam(){
      return fighters;
    }

    @Override
    public String reward(){
      double t=teamel;
      double a=alliesel;
      var m=Math.min(teamel,alliesel);
      if(m<0){//ELs can be negative
        t+=-m+1;
        a+=-m+1;
      }
      goldbonus=t/(a+t);
      return super.reward();
    }
  }

  Town town;

  /** Constructor. */
  public Arena(Town t){
    super(DESCRIPTION,DESCRIPTION,15,20);
    town=t;
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
    if(!super.interact()||!isopen(List.of(Period.AFTERNOON,Period.EVENING),this)) return false;
    var hurt=Squad.active.members.stream()
        .filter(c->c.getnumericstatus()<Combatant.STATUSSCRATCHED).limit(1);
    if(hurt.count()>0){
      Javelin.message(NONEELIGIBLE,false);
      return false;
    }
    //TODO there needs to be a check of whether can generate opponents first, probably be instantiating the Fight first and valitaing
    //TODO use the confirm prompt to pay an entry fee
    var team=new Combatants(Squad.active.members);
    if(Javelin.prompt(CONFIRM+Javelin.group(team)+".")!='\n') return false;
    var el=ChallengeCalculator.calculateel(team);
    el=Math.min(el,town.getrank().maxpopulation);
    var f=new ArenaFight(team,el);
    /* TODO would be cool to be able to generate fights in advance so here we
     * could check if EmcounterGenerator was able to come up with something or
     * not */
    throw new StartBattle(f);
  }
}
