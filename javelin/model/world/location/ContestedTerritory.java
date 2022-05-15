package javelin.model.world.location;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.Tier;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.Siege;
import javelin.controller.content.fight.mutator.mode.Waves;
import javelin.controller.content.fight.setup.BattleSetup;
import javelin.controller.content.map.Map;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.generator.encounter.AlignmentDetector;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Incursion;
import javelin.model.world.Period.Time;
import javelin.model.world.World;
import javelin.model.world.location.town.diplomacy.quest.kill.Raid;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.old.RPG;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;

/**
 * {@link Fight}-based {@link Location}s where players join forces with local
 * units in larger-scale battles. Each time it is cleared, it's
 * {@link Fortification#targetel} raises somewhat and it restocks very quickly.
 *
 * It is meant to serve as {@link Tier#LOW} {@link World} {@link Fight}s, while
 * still having unique gameplay (larger-scale battles on {@link Terrain}
 * {@link Map}s). As they grow in power, they continue to be relevant later on.
 *
 * They are also useful for soaking up {@link Incursion}s.
 *
 * Originally introduced as targets for low-level {@link Raid}s.
 *
 * @author alex
 */
public class ContestedTerritory extends Fortification{
  static final String PREPARE="""
      %s
      %s

      This is a disputed territory. If you help a side take control, you may be able to hire any survivors as mercenaries.

      The attackers are:
      %s

      The defenders are:
      %s

      Would you like to:
       [1] fight alongside the attackers (%s)?
       [2] fight alongside the defenders (%s)?
       [3] fight both sides at once (%s)?
       [a] attempt a peaceful resolution (%s)?
       [q] quit without fighting?
      """;
  static final String KEYS="123aq";
  static final String NEGOTIATE="""
      %s spends a day trying to mediate peace in the %s...
      %s
      """;

  static class Army extends ArrayList<Combatants>{
    Army(int nsquads){
      super(nsquads);
    }

    Combatants rasterize(){
      var army=new Combatants(stream().mapToInt(Combatants::size).sum());
      for(var combatants:this) army.addAll(combatants);
      return army;
    }

    void survive(List<Combatant> survivors){
      for(var a:this) a.retainAll(survivors);
    }

    @Override
    public String toString(){
      return stream().map(c->"  "+c).collect(joining("\n"));
    }

    public Combatant lead(){
      return rasterize().stream().filter(l->l.source.think(-1))
          .sorted((a,b)->-Integer.compare(a.source.charisma,b.source.charisma))
          .findFirst().orElse(null);
    }

    boolean validate(){
      var army=rasterize();
      return !army.isEmpty()&&lead()!=null&&new AlignmentDetector(army).check();
    }

    boolean raisesquads(int nsquads,int squadel,Terrain t){
      for(var i=0;i<nsquads;i++){
        var e=EncounterGenerator.generate(squadel,t);
        if(e==null) return false;
        add(e);
      }
      return true;
    }

    void raise(int nsquads,int squadel,Terrain t){
      var correction=squadel>Tier.EPIC.maxlevel?-1:+1;
      for(var tries=0;!validate();tries++){
        clear();
        if(tries%10==0) squadel+=correction;
        if(!raisesquads(nsquads,squadel,t)) clear();
      }
    }

    static Army raise(int el,Terrain t){
      var nsquads=Tier.get(el).getordinal()+1;
      var a=new ContestedTerritory.Army(nsquads);
      a.raise(nsquads,el-Waves.ELMODIFIER.get(nsquads),t);
      return a;
    }
  }

  static class TerritorySetup extends BattleSetup{
    TerritoryFight f;

    TerritorySetup(TerritoryFight f){
      this.f=f;
    }

    @Override
    public void setup(){
      var attackers=f.attackers.rasterize();
      for(var a:attackers) a.setmercenary(true);
      Fight.state.blueteam.addAll(attackers);
      super.setup();
    }
  }

  static class TerritoryFight extends Siege{
    ContestedTerritory territory;
    Army attackers;
    Army defenders;

    TerritoryFight(Army attackers,Army defenders,ContestedTerritory t){
      super(t);
      this.attackers=attackers;
      this.defenders=defenders;
      territory=t;
      setup=new TerritorySetup(this);
      bribe=false;
    }

    @Override
    public ArrayList<Combatant> getfoes(Integer el){
      return defenders.rasterize();
    }

    @Override
    public String reward(){
      double s=Squad.active.getel();
      var all=s+attackers.rasterize().getel();
      goldbonus=s/all;
      xpbonus=s/all;
      return super.reward();
    }

    @Override
    public boolean onend(){
      var result=super.onend();
      territory.defenders=defenders;
      defenders.survive(Fight.state.redteam);
      territory.attackers=attackers;
      var survivors=new Combatants(Fight.state.blueteam);
      survivors.addAll(Fight.state.fleeing);
      attackers.survive(survivors);
      for(var a:attackers.rasterize()) a.setmercenary(false);
      return result;
    }
  }

  Class<? extends Map> map;
  Army attackers;
  Army defenders;

  /** Constructor. */
  public ContestedTerritory(Map m){
    super("Contested "+m.name.toLowerCase(),"Contested territory",1,4);
    map=m.getClass();
  }

  @Override
  public List<Combatant> getcombatants(){
    var a=attackers==null?Collections.EMPTY_LIST:attackers.rasterize();
    var d=defenders==null?Collections.EMPTY_LIST:defenders.rasterize();
    return List.of(a,d).stream().flatMap(List::stream).toList();
  }

  @Override
  public void generategarrison(){
    targetel=RPG.r(minlevel,maxlevel);
    var t=Terrain.get(x,y);
    defenders=Army.raise(targetel,t);
    garrison=defenders.rasterize();
    attackers=Army.raise(targetel,t);
    if(!AlignmentDetector.antagonize(garrison,attackers.rasterize()))
      attackers=Army.raise(targetel,t);
  }

  @Override
  public void turn(long time,WorldScreen s){
    super.turn(time,s);
    if(RPG.chancein(ishostile()?Time.SEASON:Time.MONTH)){
      raiselevel(RPG.r(1,4));
      generategarrison();
    }
  }

  /** @return All {@link World} instances. */
  public static List<ContestedTerritory> getall(){
    return World.getall(ContestedTerritory.class).stream()
        .map(t->(ContestedTerritory)t).toList();
  }

  void negotiate(Combatant diplomat,int dc){
    if(diplomat==null){
      var fail="None of your allies is capable of negoatiating peace.";
      Javelin.message(fail,false);
      return;
    }
    Lodge.rest(1,24,true,Lodge.LODGE);
    String result;
    if(diplomat.roll(Skill.DIPLOMACY)>=dc){
      result="Peace is achieved!";
      attackers.addAll(defenders);
      defenders.clear();
      garrison.clear();
    }else result="Sadly, no resuolution is reached.";
    var d=descriptionknown.toLowerCase();
    Javelin.message(NEGOTIATE.formatted(diplomat,d,result),false);
  }

  TerritoryFight fight(char i){
    if(i=='1') return new TerritoryFight(attackers,defenders,this);
    if(i=='2') return new TerritoryFight(defenders,attackers,this);
    var both=new Army(attackers.size()+defenders.size());
    both.addAll(attackers);
    both.addAll(defenders);
    return new TerritoryFight(new Army(0),both,this);
  }

  void prepare(){
    var squad=Squad.active.members;
    var s=new InfoScreen("");
    var a=attackers.rasterize();
    var d=defenders.rasterize();
    var attackodds=Difficulty.describemany(List.of(squad,a),List.of(d));
    var defenddds=Difficulty.describemany(List.of(squad,d),List.of(a));
    var bothodds=Difficulty.describemany(List.of(squad),List.of(a,d));
    var leadera=attackers.lead().source.charisma;
    var leaderd=defenders.lead().source.charisma;
    var diplomacydc=20+Monster.getbonus(Math.max(leadera,leaderd));
    var diplomat=Squad.active.getbest(Skill.DIPLOMACY);
    var diplomacyodds="impossible";
    if(diplomat!=null)
      diplomacyodds=Skill.DIPLOMACY.describe(diplomacydc,diplomat);
    var underline="=";
    while(underline.length()<descriptionknown.length()) underline+="=";
    s.print(PREPARE.formatted(descriptionknown,underline,attackers,defenders,
        attackodds,defenddds,bothodds,diplomacyodds));
    var i=' ';
    while(KEYS.indexOf(i)<0) i=s.getinput();
    if(i=='a') negotiate(diplomat,diplomacydc);
    else if(i!='q') throw new StartBattle(fight(i));
  }

  void hire(){
    if(attackers.rasterize().isEmpty()){
      Javelin.message("No units are currently available for hire...",false);
      return;
    }
    var attackers=this.attackers.stream().filter(a->!a.isEmpty()).toList();
    var mercenaries=new ArrayList<String>(attackers.size());
    for(var a:attackers){
      var fee=0;
      for(var mercenary:a) fee+=MercenariesGuild.getfee(mercenary.source);
      mercenaries.add("%s ($%s/day)".formatted(a,Javelin.format(fee)));
    }
    var i=Javelin.choose("Hire which mercenary band?",mercenaries,true,false);
    if(i<0) return;
    for(var a:attackers.get(i)){
      a.setmercenary(true);
      Squad.active.add(a);
    }
    attackers.remove(i);
  }

  @Override
  public boolean interact(){
    descriptionunknown=descriptionknown;
    if(ishostile()) prepare();
    else hire();
    return true;
  }
}
