package javelin.view.mappanel.battle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javelin.Javelin;
import javelin.controller.content.action.Examine;
import javelin.controller.content.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.old.Interface;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.battle.action.BattleMouseAction;
import javelin.view.mappanel.battle.action.MeleeMouseAction;
import javelin.view.mappanel.battle.action.MoveMouseAction;
import javelin.view.mappanel.battle.action.RangedMouseAction;
import javelin.view.mappanel.overlay.Overlay;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.StatisticsScreen;

/**
 * Handles mouse events for {@link BattleScreen}.
 *
 * @author alex
 */
public class BattleMouse extends Mouse{
  static final BattleMouseAction[] ACTIONS={new MoveMouseAction(),
      new MeleeMouseAction(),new RangedMouseAction()};

  /** Constructor. */
  public BattleMouse(MapPanel panel){
    super(panel);
  }

  @Override
  public void mouseClicked(MouseEvent e){
    if(overrideinput()||!Interface.userinterface.waiting) return;
    var t=gettile(e);
    var s=Fight.state;
    var target=s.getcombatant(t.x,t.y);
    var button=e.getButton();
    if(button==MouseEvent.BUTTON3&&target!=null){
      BattleScreen.perform(()->{
        if(!target.source.passive) new StatisticsScreen(target);
      });
      return;
    }
    if(button==MouseEvent.BUTTON1){
      click(target,s);
      return;
    }
    super.mouseClicked(e);
  }

  void click(Combatant target,BattleState s){
    var current=BattlePanel.current;
    var action=getaction(s,target,current);
    var outcome=action==null?null:action.act(current,target,s);
    if(outcome!=null) BattleScreen.perform(outcome);
    if(MapPanel.overlay!=null&&(action==null||action.clearoverlay))
      MapPanel.overlay.clear();
  }

  public BattleMouseAction getaction(BattleState s,Combatant target,
      Combatant current){
    var custom=target==null?null:target.getmouseaction();
    if(custom!=null&&custom.validate(current,target,s)) return custom;
    for(BattleMouseAction a:ACTIONS) if(a.validate(current,target,s)) return a;
    return null;
  }

  @Override
  public void mouseMoved(MouseEvent e){
    if(Examine.lastlooked!=null){
      Examine.lastlooked=null;
      BattleScreen.active.statuspanel.repaint();
    }
    if(!Interface.userinterface.waiting) return;
    if(MapPanel.overlay!=null) MapPanel.overlay.clear();
    BattleScreen.active.messagepanel.clear();
    try{
      var t=gettile(e);
      var s=Fight.state;
      var current=s.clone(BattlePanel.current);
      var target=s.getcombatant(t.x,t.y);
      var action=getaction(s,target,current);
      if(action==null){
        var status=target+" ("+target.getstatus()+")";
        showstatus(status,target,null);
      }else action.onenter(current,target,t,s);
      if(target!=null&&(action==null||action.clearoverlay)){
        Examine.lastlooked=target;
        BattleScreen.active.statuspanel.repaint();
      }
    }finally{
      MessagePanel.active.repaint();
    }
  }

  public static void showstatus(String status,Combatant c,Overlay o){
    if(o!=null){
      MapPanel.overlay=o;
      Javelin.redraw();
    }
    status+="\n\nConditions: ";
    var list=c.printstatus(Fight.state);
    status+=list.isEmpty()?"none":list;
    Javelin.message(status+=".",Javelin.Delay.NONE);
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e){
    if(!Interface.userinterface.waiting) return;
    super.mouseWheelMoved(e);
    var current=BattlePanel.current;
    BattleScreen.active.mappanel.center(current.location[0],current.location[1],
        true);
  }
}
