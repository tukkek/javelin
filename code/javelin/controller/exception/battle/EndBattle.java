package javelin.controller.exception.battle;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ThreadManager;
import javelin.controller.ai.cache.AiCache;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.mutator.Friendly;
import javelin.controller.content.terrain.Terrain;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.conjuration.healing.raise.Ressurect;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.ReviveScreen;

/**
 * A victory or defeat condition has been achieved.
 *
 * @author alex
 */
public class EndBattle extends BattleEvent{
  /** Start after-{@link Fight} cleanup. */
  public static void end(){
    var f=Fight.current;
    Fight.victory=f.win();
    terminateconditions(Fight.state,BattleScreen.active);
    if(f.onend()){
      var s=Squad.active;
      if(s!=null){
        while(World.get(s.x,s.y,Incursion.class)!=null){
          s.displace();
          s.place();
        }
        end(Fight.originalblueteam);
        if(Dungeon.active!=null) Dungeon.active.enter();
      }
    }
    AiCache.reset();
  }

  static void terminateconditions(BattleState s,BattleScreen screen){
    screen.block();
    for(Combatant c:Fight.state.getcombatants()) c.finishconditions(s,screen);
  }

  /** * Prints combat info (rewards, etc). */
  public static void resolve(){
    MessagePanel.active.clear();
    var result="";
    var s=Fight.state;
    var f=Fight.current;
    if(Fight.victory) result=f.reward();
    else
      if(f.has(Friendly.class)!=null&&!s.blueteam.isEmpty()) result="You lost!";
      else if(s.getfleeing(Fight.originalblueteam).isEmpty()){
        Squad.active.disband();
        result="You lost!";
      }else{
        result="Fled from combat. No awards received.";
        if(!Fight.victory&&s.fleeing.size()!=Fight.originalblueteam.size()){
          result+="\nFallen allies left behind are lost!";
          for(Combatant abandoned:s.dead) abandoned.hp=Combatant.DEADATHP;
        }
        if(Squad.active.transport!=null&&Dungeon.active==null
            &&!Terrain.current().equals(Terrain.WATER)){
          result+=" Vehicle lost!";
          Squad.active.transport=null;
          Squad.active.updateavatar();
        }
      }
    if(result!=null&&!f.skipresult) Javelin.message(result,true);
  }

  static void updateoriginal(List<Combatant> originalteam){
    var update=new ArrayList<>(Fight.state.blueteam);
    for(var inbattle:update){
      var originali=originalteam.indexOf(inbattle);
      if(originali>=0){
        inbattle.terminateconditions(0);
        inbattle.xp=originalteam.get(originali).xp;
        originalteam.set(originali,inbattle);
      }
    }
  }

  static void copyspells(final Combatant from,final Combatant to){
    for(var spell:from.spells){
      var original=to.spells.get(spell);
      if(original!=null) original.used=spell.used;
    }
  }

  /**
   * Tries to {@link #show(Combatant)} the combatant. If can't, remove him from
   * the game.
   *
   * TODO isn't updating {@link Ressurect#dead} when the entire Squad dies! this
   * probably isn't being called
   */
  static void bury(List<Combatant> originalteam){
    for(Combatant c:Fight.state.dead){
      if(!originalteam.contains(c)) continue;
      if(c.hp>Combatant.DEADATHP&&c.source.constitution>0){
        c.hp=1;
        Fight.state.blueteam.add(c);
      }else if(!Fight.victory||!new ReviveScreen(originalteam).show(c)){
        originalteam.remove(c);
        c.bury();
      }
    }
    Fight.state.dead.clear();
  }

  static void end(Combatants originalteam){
    var s=Squad.active.members;
    originalteam.retainAll(s);
    var state=Fight.state;
    state.fleeing.retainAll(s);
    state.dead.retainAll(s);
    state.blueteam.retainAll(s);
    bury(originalteam);
    updateoriginal(originalteam);
    Squad.active.members=originalteam;
    ThreadManager.printbattlerecord();
  }
}
