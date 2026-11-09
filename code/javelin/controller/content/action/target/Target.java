package javelin.controller.content.action.target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;

import javelin.Javelin;
import javelin.controller.content.action.Action;
import javelin.controller.content.fight.Fight;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.StatusPanel;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.battle.overlay.TraceOverlay;
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
  static final char NEXT='+';
  static final char GO='\n';
  static final char SHOW='s';
  static final char LAST='-';

  class SelectTarget implements Comparator<Combatant>{
    Combatant c;
    BattleState state;
    private Target action;

    SelectTarget(Combatant c,BattleState state,Target action){
      this.c=c;
      this.state=state;
      this.action=action;
    }

    @Override
    public int compare(Combatant o1,Combatant o2){
      var priority1=action.prioritize(c,o1);
      var priority2=action.prioritize(c,o2);
      if(priority1!=priority2) return priority1>priority2?-1:1;
      var distance1=Walker.distance(o1,c)*10;
      var distance2=Walker.distance(o2,c)*10;
      return Math.round(Math.round(distance1-distance2));
    }
  }

  /// Pressing this key confirms the target selection, usually same as the
  /// action key.
  ///
  /// @see Action#keys
  protected char confirmkey;

  int index=0;
  Combatant target=null;

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
    select(combatant,targets,state);
    return true;
  }

  boolean navigate(char key){
    if(key==NEXT||Action.MOVE_E.isPressed(key)){
      index+=1;
      return true;
    }
    if(key==LAST||Action.MOVE_W.isPressed(key)){
      index-=1;
      return true;
    }
    return false;
  }

  boolean show(char key){
    if(key!=SHOW) return false;
    new StatisticsScreen().open(target);
    throw new RepeatTurn();
  }

  boolean attack(char key,Combatant active,BattleState s){
    if(key!=GO&&key!=confirmkey) return false;
    Overlay.clear();
    MessagePanel.active.clear();
    attack(active,target,s);
    return true;
  }

  void target(List<Combatant> targets){
    // wrap index
    var size=targets.size();
    if(index>=size) index=0;
    else if(index<0) index=size-1;
    // target
    target=targets.get(index);
  }

  void select(Combatant active,List<Combatant> targets,BattleState s){
    index=0;
    target=null;
    while(true){
      target(targets);
      lock(active,s);
      Javelin.redraw();
      var key=InfoScreen.feedback();
      if(attack(key,active,s)) break;
      if(navigate(key)||show(key)) continue;
      StatusPanel.looking=null;
      Javelin.redraw();
      throw new RepeatTurn();
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
  protected Overlay overlay(Combatant active,Combatant target){
    return new TraceOverlay(active.getlocation(),target.getlocation());
  }

  void lock(Combatant active,BattleState s){
    Overlay.set(overlay(active,target));
    var point=target.getlocation();
    BattleScreen.active.center(point.x,point.y);
    StatusPanel.looking=target;
    MessagePanel.active.clear();
    var prompt="Use ← and → to select target, ENTER or "+confirmkey
        +" to confirm, s to show statistics, q to quit.\n\n";
    prompt+=describehitchance(active,target,s);
    Javelin.message(prompt,Javelin.Delay.NONE);
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
  public int prioritize(Combatant c,Combatant target){
    return -target.getlocation().distanceinsteps(c.getlocation());
  }
}
