package javelin.test;

import java.util.ArrayList;
import java.util.List;

import javelin.Debug;
import javelin.Debug.DebugFight;
import javelin.controller.collection.CountingSet;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.mutator.Mutator;
import javelin.controller.content.fight.setup.BattleSetup;
import javelin.controller.content.map.Map;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;

/** Tests {@link Map}s. */
public class TestMap{
  static final Mutator END=new Mutator(){
    @Override
    public void ready(Fight f){
      Fight.state.redteam.clear();
      throw new EndBattle();
    }
  };

  static Combatants makearmy(int nopponents){
    var monsters=new Combatants(nopponents);
    while(monsters.size()<nopponents)
      monsters.add(new Combatant(Monster.get("Orc"),true));
    return monsters;
  }

  /** Put Fight.withdrawall(false) on {@link Debug#onbattlestart()}. */
  static void place(Integer times,List<? extends Class<? extends Map>> maps){
    try{
      var measures=new ArrayList<Long>(times*maps.size());
      var passes=new CountingSet();
      var opponents=makearmy(100);
      for(var map:maps) for(var i=1;i<=times;i++){
        System.out.println(map.getCanonicalName()+" "+i+"/"+times);
        var f=new DebugFight(opponents);
        f.avoid=false;
        f.map=map.getConstructor().newInstance();
        f.skipresult=true;
        f.mutators.add(END);
        Fight.current=f;
        var clock=System.currentTimeMillis();
        try{
          new StartBattle(f).battle();
        }catch(EndBattle e){
          EndBattle.end();
        }
        measures.add(System.currentTimeMillis()-clock);
        passes.add(BattleSetup.pass);
      }
      measures.sort(null);
      System.out.println("Passes: "+passes+"\nMedian time: "
          +measures.get(measures.size()/2)+"ms");
    }catch(ReflectiveOperationException e){
      throw new RuntimeException(e);
    }
  }

  /** Call from {@link Debug#onworldhelp()}. */
  public static void test(List<? extends Class<? extends Map>> maps){
    place(60,maps);
  }
}
