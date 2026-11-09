package javelin.view.mappanel.battle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javelin.Javelin;
import javelin.controller.content.fight.Fight;
import javelin.controller.exception.RepeatTurn;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.old.Interface;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.OpenGL;
import javelin.view.StatusPanel;
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

  void show(Combatant c){
    BattleScreen.perform(()->{
      new StatisticsScreen().open(c);
      throw new RepeatTurn();
    });
  }

  @Override
  public void mouseClicked(MouseEvent e){
    super.mouseClicked(e);
    synchronized(OpenGL.LOCK){
      if(overrideinput()) return;
      var t=gettile(e);
      if(t==null) return;
      var s=Fight.state;
      var target=s.getcombatant(t.x,t.y);
      if(target!=null) switch(e.getButton()){
        case MouseEvent.BUTTON1 -> click(target,s);
        case MouseEvent.BUTTON3 -> show(target);
      }
    }
  }

  void click(Combatant target,BattleState s){
    var current=BattlePanel.current;
    var action=getaction(s,target,current);
    var outcome=action==null?null:action.act(current,target,s);
    if(outcome!=null) BattleScreen.perform(outcome);
    if(action==null||action.clearoverlay) Overlay.clear();
  }

  public BattleMouseAction getaction(BattleState s,Combatant target,
      Combatant current){
    for(BattleMouseAction a:ACTIONS) if(a.validate(current,target,s)) return a;
    return null;
  }

  @Override
  public void mouseMoved(MouseEvent e){
    synchronized(OpenGL.LOCK){
      var statuspanel=StatusPanel.get();
      if(StatusPanel.looking!=null){
        StatusPanel.looking=null;
        statuspanel.repaint();
      }
      if(!Interface.userinterface.waiting) return;
      Overlay.clear();
      MessagePanel.get().clear();
      try{
        var t=gettile(e);
        if(t==null) return;
        var s=Fight.state;
        var current=s.clone(BattlePanel.current);
        var target=s.getcombatant(t.x,t.y);
        var action=getaction(s,target,current);
        if(action==null){
          var status=target+" ("+target.getstatus()+")";
          showstatus(status,target,null);
        }else action.onenter(current,target,t,s);
        if(target!=null&&(action==null||action.clearoverlay)){
          StatusPanel.looking=target;
          statuspanel.repaint();
        }
      }finally{
        MessagePanel.active.repaint();
      }
    }
  }

  public static void showstatus(String status,Combatant c,Overlay o){
    if(o!=null) Overlay.set(o);
    status+="\n\nConditions: ";
    var list=c.printstatus(Fight.state);
    status+=list.isEmpty()?"none":list;
    Javelin.message(status+=".",Javelin.Delay.NONE);
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e){
    synchronized(OpenGL.LOCK){
      if(!Interface.userinterface.waiting) return;
      super.mouseWheelMoved(e);
      var current=BattlePanel.current;
      MapPanel.get().center(current.location[0],current.location[1],true);
    }
  }
}
