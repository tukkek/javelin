package javelin.controller.content.action.target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;

import javelin.Javelin;
import javelin.controller.content.action.Action;
import javelin.controller.content.action.Examine;
import javelin.controller.content.fight.Fight;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Combatant;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.battle.overlay.TargetOverlay;
import javelin.view.mappanel.overlay.Overlay;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.StatisticsScreen;

/**
 * Base class for all actions involving selecting an unit as target.
 *
 * @author alex
 */
public abstract class Target extends Action{
  public class SelectTarget implements Comparator<Combatant>{
    Combatant c;
    BattleState state;
    private Target action;

    public SelectTarget(Combatant c,BattleState state,Target action){
      this.c=c;
      this.state=state;
      this.action=action;
    }

    @Override
    public int compare(Combatant o1,Combatant o2){
      var priority1=action.prioritize(c,state,o1);
      var priority2=action.prioritize(c,state,o2);
      if(priority1!=priority2) return priority1>priority2?-1:1;
      var distance1=Walker.distance(o1,c)*10;
      var distance2=Walker.distance(o2,c)*10;
      return Math.round(Math.round(distance1-distance2));
    }
  }

  /**
   * Pressing this key confirms the target selection, usually same as the action
   * key.
   *
   * @see Action#keys
   */
  protected char confirmkey;

  /** Constructor. */
  public Target(String string){
    super(string);
  }

  /** Constructor. */
  public Target(String string,String[] strings){
    super(string,strings);
  }

  /** Constructor. */
  public Target(String name,String key){
    super(name,key);
  }

  /**
   * Used for descriptive purposes.
   *
   * @return Minimum number the active combatant has to roll on a d20 to hit the
   *   target.
   */
  protected abstract int predictchance(Combatant c,Combatant target,
      BattleState s);

  /** Called once a target is confirmed. */
  protected abstract void attack(Combatant c,Combatant target,BattleState s);

  @Override
  public boolean perform(Combatant c){
    checkhero(c);
    var state=Fight.state.clone();
    if(checkengaged(state,state.clone(c))){
      MessagePanel.active.clear();
      Javelin.message("Disengage first...",Javelin.Delay.WAIT);
      throw new RepeatTurn();
    }
    var combatant=state.clone(c);
    List<Combatant> targets=state.getcombatants();
    filtertargets(combatant,targets,state);
    targets=state.gettargets(combatant,targets);
    if(targets.isEmpty()){
      MessagePanel.active.clear();
      Javelin.message("No valid targets...",Javelin.Delay.WAIT);
      throw new RepeatTurn();
    }
    Collections.sort(targets,new SelectTarget(c,state,this));
    selecttarget(combatant,targets,state);
    return true;
  }

  /** TODO rename to select() */
  void selecttarget(Combatant c,List<Combatant> targets,BattleState state){
    var targeti=0;
    lock(c,targets.get(0),state);
    while(true){
      Javelin.redraw();
      var key=InfoScreen.feedback();
      if(Action.MOVE_W.isPressed(key)||key=='-') targeti-=1;
      else if(Action.MOVE_E.isPressed(key)||key=='+') targeti+=1;
      else if(key=='\n'||key==confirmkey){
        if(MapPanel.overlay!=null) MapPanel.overlay.clear();
        MessagePanel.active.clear();
        attack(c,targets.get(targeti),state);
        break;
      }else if(key=='v'&&!targets.get(targeti).source.passive)
        new StatisticsScreen(targets.get(targeti));
      else{
        Examine.lastlooked=null;
        Javelin.redraw();
        throw new RepeatTurn();
      }
      var max=targets.size()-1;
      if(targeti>max) targeti=0;
      else if(targeti<0) targeti=max;
      lock(c,targets.get(targeti),state);
    }
  }

  /**
   * By default uses {@link BattleState#isengaged(Combatant)}
   *
   * @return <code>true</code> if the active unit is currently engaded and
   *   should not be allowed to continue targetting.
   */
  protected boolean checkengaged(BattleState state,Combatant c){
    return state.isengaged(c);
  }

  /**
   * Does nothing by default.
   *
   * @param hero Active unit.
   * @throws RepeatTurn
   */
  protected void checkhero(Combatant hero){

  }

  /**
   * By default only allows targeting enemies that are in line-of-sight.
   *
   * @param targets Remove invalid targets from this list. Beware of
   *   {@link ConcurrentModificationException}.
   */
  protected void filtertargets(Combatant active,List<Combatant> targets,
      BattleState s){
    for(Combatant target:new ArrayList<>(targets))
      if(target.isally(active,s)) targets.remove(target);
  }

  /** @return Assigns to {@link MapPanel#overlay}. */
  protected Overlay overlay(Combatant target){
    return new TargetOverlay(target.getlocation());
  }

  void lock(Combatant active,Combatant target,BattleState s){
    if(MapPanel.overlay!=null) MapPanel.overlay.clear();
    MapPanel.overlay=overlay(target);
    MessagePanel.active.clear();
    var prompt="Use ← and → to select target, ENTER or "+confirmkey
        +" to confirm, v to view target's sheet, q to quit.\n\n";
    prompt+=describehitchance(active,target,s);
    Javelin.message(prompt,Javelin.Delay.NONE);
    Examine.lastlooked=target;
    BattleScreen.active.center(target.location[0],target.location[1]);
  }

  /** @return Text with the name of the target and chance to hit. */
  public String describehitchance(Combatant c,Combatant target,BattleState s){
    var status=new ArrayList<String>(2);
    status.add(target.getstatus());
    status.add(Javelin.getchance(predictchance(c,target,s))+" to hit");
    status.addAll(target.liststatus(s));
    return target+" ("+String.join(", ",status)+")";
  }

  /** @return Higher value means earlier in {@link Target} order. */
  public int prioritize(Combatant c,BattleState state,Combatant target){
    var priority=target.getlocation().distanceinsteps(c.getlocation());
    if(state.haslineofsight(c,target)==Vision.COVERED) priority-=10;
    if(state.isengaged(target)) priority-=100;
    return priority;
  }
}
