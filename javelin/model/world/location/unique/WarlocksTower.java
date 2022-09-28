package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.Weather;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.challenge.Tier;
import javelin.controller.collection.Kits;
import javelin.controller.comparator.KitsByName;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.mutator.Mutator;
import javelin.controller.content.fight.setup.BattleSetup;
import javelin.controller.content.kit.Kit;
import javelin.controller.content.map.Map;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.db.EncounterIndex;
import javelin.controller.exception.GaveUp;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.generator.encounter.EncounterGenerator.MonsterPool;
import javelin.controller.table.dungeon.BranchTable;
import javelin.model.item.Item;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.unit.condition.GenericBuff;
import javelin.model.world.Period;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.feature.rare.Fountain;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;

/**
 * This {@link Location} is meant as power-leveling/"speed-running" tool:
 * extremely {@link Difficulty#DEADLY} {@link Fight}s that offer a 50-50% chance
 * of winning. Each round adds a summoned ally in the player's control and an
 * opportunity to leave. Should the player win, the rewards are plentiful and
 * one of the summoned allies can be kept in your {@link Squad}.
 *
 * The Tower is near the starting {@link Town} and it scales and can be done
 * multiple times - making it a good challenge. Upon winning, the player is
 * allowed to upgrade any units with a given number of {@link Kit}s.
 *
 * {@link Fight}s {@link Map}s are based on a random {@link Terrain}, while
 * {@link Branch}es are used to make each Fight more interesting and unique.
 *
 * TODO Other {@link Branch} stuff ({@link Mutator}s, etc) TODO
 *
 * @author alex
 */
public class WarlocksTower extends Location{
  static final String DESCRIPTION="The Warlock's Tower";
  static final String ENTER="""
      Are you sure you wish to submit yourself to the Warlock's challenge? You are unlikely to survive...

      Press s to submit to the trial or any other key to continue...
      """;
  static final String CONCEDE="""
      "Do you concede, mortal? I will let you leave but any fallen allies will be mine!"

      Press c to concede or s to stay and fight...
      """;
  static final String UPGRADE="""
      The Warlock offers to magically train your units. What set of skills should they learn?

      %s

      Press the assigned keys to cycle between upgrades.
      Press q to quit without spending experience points.
      Press ENTER to confirm.
      """;
  static final List<Character> UPGRADEKEYS=SelectScreen.filterkeys("q");
  static final int ELMODIFIER=-1;
  static final int INDEXSIZE=9;
  static final int KITS=2;

  /**
   * TODO
   *
   * Uses {@link WarlocksBlessing} to buff {@link Squad} heroes, as they become
   * fairly useless later on and are expected to fall otherwise (or
   * regardless!), more likely than not. Even a +1 {@link GenericBuff} can be
   * pretty meaningful though, especially stacked (-1 would equal an entire
   * negative level). Ideally would not be necessary but can be turned on to
   * provide an advantage.
   */
  //  static final boolean BLESS=false;
  //  class WarlocksBlessing extends GenericBuff{
  //    public WarlocksBlessing(Combatant c){
  //      super(c,+1,"Warlock's blessing",0);
  //      effect=Effect.NEUTRAL;
  //    }
  //  }

  class WarlockWaves extends Mutator{
    //    List<Monster> enemies=new ArrayList<>();
    Combatants allies=new Combatants();
    int waves=1;
    int wave=1;

    @Override
    public void ready(Fight f){
      super.ready(f);
      //      for(var r:Fight.state.redteam) enemies.add(r.source);
      for(var b:Fight.state.blueteam) Fountain.heal(b);
      while(RPG.chancein(2)) waves+=1;
    }

    Combatants reinforce(){
      //      if(crp<1) crp=1;
      //      var candidates=new Combatants();
      //      for(var cr=crp;candidates.size()<3;cr--){
      //        var cr2=cr;
      //        var recruits=f.monsters.stream().filterindex(m->m.source.cr==cr2)
      //            .collect(Collectors.toList());
      //        if(recruits!=null) candidates.addAll(recruits);
      //      }
      var dead=Fight.state.dead;
      var reinforce=new Combatants(dead);
      reinforce.retainAll(Fight.originalblueteam);
      dead.removeAll(reinforce);
      var candidates=dead.stream().filter(d->!d.summoned).toList();
      var r=new Combatant(RPG.pick(candidates).source,true);
      //      r.summoned=true;
      reinforce.add(r);
      Fight.originalblueteam.add(r);
      Squad.active.add(r);
      allies.add(r);
      dead.remove(r);
      //      while(ChallengeCalculator.calculatecr(r.source)<crp)
      //        if(!Commoner.SINGLETON.upgrade(r)) break;
      return reinforce;
    }

    void lose(){
      var state=Fight.state;
      if(!state.blueteam.isEmpty()) return;
      var squad=Squad.active;
      if(!allies.isEmpty()){
        var mercy="\"You have failed but I'll let you live to try again!\"";
        Javelin.message(mercy,true);
        for(var a:allies){
          Fight.originalblueteam.remove(a);
          squad.remove(a);
        }
        allies.clear();
      }else if(squad.members.size()>1){
        var lose="\"The price you will pay for your loss is for %s to become one of my slaves!\"";
        var m=RPG.pick(squad.members);
        Fight.originalblueteam.remove(m);
        //        squad.remove(m);
        Javelin.message(lose.formatted(m),true);
      }else return;
      Fight.victory=false;
      Fight.current.rewardxp=false;
      Fight.current.skipresult=true;
      state.fleeing.addAll(Fight.originalblueteam);
      throw new EndBattle();
    }

    @Override
    public void checkend(Fight f){
      lose();
      super.checkend(f);
      var s=Fight.state;
      if(!s.redteam.isEmpty()) return;
      wave+=1;
      if(wave>waves) return;
      for(var c:s.getcombatants()) if(c.summoned) s.remove(c);
      //      if(Javelin.prompt(CONCEDE,Set.of('c','s'))=='c'){
      //        //        s.redteam.addAll(Combatants.from(enemies));
      //        f.flee(false);
      //        return;
      //      }

      //      if(BLESS) for(var c:s.blueteam) if(Squad.active.members.contains(c))
      //        c.addcondition(new WarlocksBlessing(c));
      var r=reinforce();
      var blue=s.blueteam;
      f.add(r,blue);
      //      allies.add(a);
      for(var b:blue) Fountain.heal(b);
      Javelin.redraw();
      var el=ChallengeCalculator.calculateel(blue)+ELMODIFIER;
      //      for(var foe:f.getfoes(Math.round(a.source.cr))){
      //        enemies.add(foe.source);
      //        var crs=enemies.stream().map(e->e.cr).toList();
      //        if(ChallengeCalculator.calculateelfromcrs(crs)>=el) break;
      //      }
      f.add(new Combatants(f.getfoes(el)),s.redteam);
    }
  }

  class KitSelection extends HashMap<Combatant,Kit>{
    KitSelection(int size){
      super(size);
    }
  }

  class Setup extends BattleSetup{
    void wall(Square a){
      a.clear();
      a.blocked=true;
    }

    @Override
    public Map generatemap(Fight f,Map m){
      m=super.generatemap(f,m);
      m.floor=Images.get(List.of("terrain","warlockfloor"));
      m.wall=Images.get(List.of("terrain","warlockwall"));
      var width=m.map.length;
      var height=m.map[0].length;
      for(var x=0;x<width;x++){
        var walls=Math.max(1,Math.abs(width/2-x)*3/5);
        for(var y=0;y<walls;y++){
          if(!RPG.chancein(3)) wall(m.map[x][y]);
          if(!RPG.chancein(3)) wall(m.map[x][height-1-y]);
        }
      }
      for(var y=0;y<height;y++){
        wall(m.map[0][y]);
        wall(m.map[width-1][y]);
      }
      return m;
    }
  }

  class WarlockChallenge extends Fight{
    //    List<Combatant> monsters=new ArrayList<>();
    EncounterIndex index=new EncounterIndex();
    WarlockWaves waves=new WarlockWaves();

    WarlockChallenge(){
      bribe=false;
      hide=false;
      rewardgold=false;
      period=Period.EVENING;
      canflee=false;
      mutators.add(waves);
      var el=Squad.active.getel()+ELMODIFIER;
      var nbranches=RPG.r(0,Tier.get(el).getordinal()+2);
      List<Branch> branches=new ArrayList<>(BranchTable.BRANCHES);
      branches=RPG.shuffle(branches).subList(0,nbranches);
      Terrain t=null;
      for(var b:branches){
        if(!b.terrains.isEmpty()) t=RPG.pick(b.terrains);
        index.merge(b.getencounters());
      }
      if(t==null) t=RPG.pick(Terrain.NONWATER);
      if(index.isEmpty()) index=t.getencounters();
      map=t.getmap();
      //      var mobs=t.getmonsters();
      //      monsters.addAll(index.getall());
      //      index=new EncounterIndex(mobs);
      //      for(var b:branches) index.merge(b.getencounters());
      filterindex(el);
      if(index.count()<INDEXSIZE){
        index.merge(new EncounterIndex(t.getmonsters()));
        filterindex(el);
      }
      for(var e:index.getall())
        for(var b:branches) for(var template:b.templates){
          var modified=new Combatants(e.group).clone();
          if(template.apply(modified)>0) index.put(new Encounter(modified));
        }
      setup=new Setup();
    }

    void filterindex(int el){
      index=index.filter(el+Difficulty.EASY,el+Difficulty.DEADLY)
          .limit(INDEXSIZE);
    }

    @Override
    public Integer getel(int teamel){
      return Squad.active.getel();
    }

    @Override
    public ArrayList<Combatant> getfoes(Integer el){
      try{
        return new MonsterPool(index).generate(el);
      }catch(GaveUp e){
        throw new RuntimeException(e);
      }
    }

    void loot(){
      var dead=new Combatants(Fight.state.dead);
      dead.retainAll(Fight.originalredteam);
      var gold=0;
      for(var d:dead) gold+=RewardCalculator.getgold(d.source.cr);
      var nmembers=Squad.active.members.size();
      var nitems=RPG.randomize(nmembers*2,1,Integer.MAX_VALUE);
      for(var i:RewardCalculator.generateloot(gold,nitems,Item.NONPRECIOUS)){
        i.identified=true;
        i.grab();
      }
    }

    String print(Combatants squad,HashMap<Combatant,Kit> selected){
      var padding=squad.stream().map(s->s.toString().length()).reduce(Math::max)
          .orElseThrow();
      var lines=new ArrayList<String>(squad.size());
      for(var i=0;i<squad.size();i++){
        var unit=squad.get(i);
        var kit=selected.get(unit);
        var name=unit.toString();
        while(name.length()<padding) name+=" ";
        var format="[%s] %s %6s %s";
        var key=UPGRADEKEYS.get(i);
        var xp=unit.gethumanxp();
        lines.add(format.formatted(key,name,xp,kit==null?"Don't train":kit));
      }
      return UPGRADE.formatted(String.join("\n",lines));
    }

    KitSelection choose(Combatants squad,HashMap<Combatant,Kits> kits){
      var selected=new KitSelection(squad.size());
      for(var s:squad){
        var choices=kits.get(s);
        selected.put(s,RPG.pick(choices.subList(0,choices.size()-1)));
      }
      var screen=new InfoScreen("");
      var input=' ';
      while(input!='q'&&input!='\n'){
        screen.print(print(squad,selected));
        input=screen.getinput();
        var k=UPGRADEKEYS.indexOf(input);
        if(k<0) continue;
        var unit=squad.get(k);
        var choices=kits.get(unit);
        var i=choices.indexOf(selected.get(unit));
        i+=1;
        if(i>=choices.size()) i=0;
        selected.put(unit,choices.get(i));
      }
      return input=='q'?null:selected;
    }

    void upgrade(){
      var squad=new Combatants(Squad.active.members.stream()
          .filter(m->m.xp.doubleValue()>=1).toList());
      if(squad.isEmpty()) return;
      var kits=new HashMap<Combatant,Kits>(squad.size());
      for(var s:squad){
        var preferred=new Kits(RPG.shuffle(Kit.getpreferred(s.source,true)));
        if(preferred.size()>KITS) preferred=new Kits(preferred.subList(0,KITS));
        preferred.sort(KitsByName.INSTANCE);
        preferred.add(null);
        kits.put(s,preferred);
      }
      var upgrades=choose(squad,kits);
      var blue=Fight.state.blueteam;
      if(upgrades!=null) for(var s:squad){
        var kit=upgrades.get(s);
        if(kit!=null){
          AdventurersGuild.train(s,kit.getupgrades(),s.xp.floatValue());
          blue.set(blue.indexOf(s),s);
        }
      }
    }

    @Override
    public boolean onend(){
      super.onend();
      var s=Fight.state;
      var dead=new ArrayList<>(s.dead);
      dead.retainAll(Fight.originalblueteam);
      s.blueteam.addAll(dead);
      for(var b:s.blueteam) Fountain.heal(b);
      if(!Fight.victory||!s.fleeing.isEmpty()) return true;
      Javelin.message("\"You... have won?\"",true);
      for(var a:waves.allies) a.source.elite=false;
      for(var d:dead) Fountain.heal(d);
      loot();
      upgrade();
      return true;
    }

    @Override
    public int flood(){
      return RPG.pick(Arrays.asList(Weather.DISTRIBUTION));
    }

    @Override
    public String reward(){
      super.reward();
      return null;
    }
  }

  /** Constructor. */
  public WarlocksTower(){
    super(DESCRIPTION);
    impermeable=true;
    allowentry=false;
  }

  @Override
  public List<Combatant> getcombatants(){
    return Collections.EMPTY_LIST;
  }

  @Override
  public boolean ishostile(){
    return false;
  }

  @Override
  public Integer getel(){
    throw new UnsupportedOperationException();//TODO
  }

  @Override
  public boolean interact(){
    if(Javelin.prompt(ENTER)!='s') return false;
    WorldScreen.current.messagepanel.clear();
    var loading="Please wait, The Warlock is setting up tactical scenario #%s...";
    loading=loading.formatted(Javelin.format(RPG.r(1,9000)));
    Javelin.message(loading,Delay.NONE);
    WorldScreen.current.messagepanel.repaint();
    throw new StartBattle(new WarlockChallenge());
  }
}
