package javelin.controller.content.action;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.content.action.ai.attack.AttackResolver;
import javelin.controller.content.action.ai.attack.MeleeAttack;
import javelin.controller.content.fight.Fight;
import javelin.controller.exception.RepeatTurn;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.mappanel.battle.overlay.TargetOverlay;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.StatisticsScreen;

/**
 * Lets the player examine the surroundings and monsters.
 *
 * @author alex
 */
public class Examine extends Action{
  final static HashMap<Character,Point> DIRECTIONS=new HashMap<>();

  static{
    DIRECTIONS.put('8',new Point(0,-1));
    DIRECTIONS.put('2',new Point(0,1));
    DIRECTIONS.put('4',new Point(-1,0));
    DIRECTIONS.put('6',new Point(1,0));
    DIRECTIONS.put('7',new Point(-1,-1));
    DIRECTIONS.put('9',new Point(1,-1));
    DIRECTIONS.put('1',new Point(-1,1));
    DIRECTIONS.put('3',new Point(1,1));
  }

  /** Last unit to be examined. */
  static public Combatant lastlooked=null;

  /** Constructor. */
  public Examine(){
    super("examine",new String[]{"x"});
    allowburrowed=true;
  }

  @Override
  public boolean perform(Combatant active){
    doLook(active,BattleScreen.active);
    throw new RepeatTurn();
  }

  /** get location, initially place crosshairs at start */
  static public Point getTargetLocation(Point start,BattleScreen s){
    startlooking(start,s);
    try{
      while(true){
        final var e=Javelin.input();
        if(e==null) continue;
        var delta=new Point(0,0);
        var key=convertkey(e);
        var cursor=getcursor();
        if(DIRECTIONS.get(key)!=null) delta=DIRECTIONS.get(key);
        else if(key=='v'){
          var target=Fight.state.getcombatant(cursor.x,cursor.y);
          if(target!=null&&!target.source.passive) new StatisticsScreen(target);
        }else if(key=='q'){
          clearCursor();
          return null;
        }else{
          clearCursor();
          return cursor;
        }
        look(delta,cursor,s);
      }
    }finally{
      lastlooked=null;
      MessagePanel.active.clear();
    }
  }

  static void look(Point delta,Point cursor,BattleScreen s){
    var x=checkbounds(cursor.x+delta.x,Fight.state.map.length);
    var y=checkbounds(cursor.y+delta.y,Fight.state.map[0].length);
    setCursor(x,y,s);
    s.mappanel.viewposition(x,y);
    doLookPoint(getcursor(),s);
  }

  static void startlooking(Point start,BattleScreen s){
    if(start==null){
      var active=Fight.state.next;
      start=new Point(active.location[0],active.location[1]);
    }
    setCursor(start.x,start.y,s);
    s.mappanel.viewposition(start.x,start.y);
    doLookPoint(getcursor(),s);
    s.statuspanel.repaint();
  }

  static void clearCursor(){
    if(MapPanel.overlay!=null) MapPanel.overlay.clear();
  }

  static Point getcursor(){
    var cursor=(TargetOverlay)MapPanel.overlay;
    return new Point(cursor.x,cursor.y);
  }

  static void setCursor(int x,int y,BattleScreen s){
    clearCursor();
    MapPanel.overlay=new TargetOverlay(x,y);
    s.mappanel.repaint();
    BattleScreen.active.mappanel.center(x,y,true);
  }

  static void doLook(Combatant active,BattleScreen s){
    doLookPoint(getTargetLocation(active.getlocation(),s),s);
  }

  static void doLookPoint(final Point p,BattleScreen s){
    if(p==null) return;
    if(!s.mappanel.tiles[p.x][p.y].discovered){
      lookmessage("Can't see",s);
      return;
    }
    lookmessage("",s);
    lastlooked=null;
    final var state=Fight.state;
    final var combatant=state.getcombatant(p.x,p.y);
    if(combatant!=null){
      var customaction=combatant.getmouseaction();
      if(customaction!=null) customaction.onenter(state.next,combatant,
          BattleScreen.active.mappanel.tiles[p.x][p.y],state);
      else{
        lookmessage(describestatus(combatant,state),s);
        lastlooked=combatant;
      }
    }else if(state.map[p.x][p.y].flooded) lookmessage("Flooded",s);
    else if(Fight.current.map.map[p.x][p.y].blocked) lookmessage("Blocked",s);
    s.statuspanel.repaint();
  }

  static void lookmessage(final String status,BattleScreen s){
    s.messagepanel.clear();
    var message="Examine: move the cursor over another combatant, press v to view character sheet.\n\n";
    Javelin.message(message+status,Javelin.Delay.NONE);
    Javelin.redraw();
  }

  static String describestatus(Combatant c,BattleState s){
    var current=BattlePanel.current;
    var status=c.printstatus(s);
    if(current.getlocation().distanceinsteps(c.getlocation())==1){
      var sequence=current.source.melee.get(0);
      var resolver=new AttackResolver(MeleeAttack.INSTANCE,current,c,
          sequence.get(0),s);
      resolver.preview(c);
      status+=", "+resolver.chance;
    }
    return status.isEmpty()?c.toString():c+" ("+status+")";
  }

  static char convertkey(final KeyEvent e){
    switch(e.getKeyCode()){
      case KeyEvent.VK_UP:
        return '8';
      case KeyEvent.VK_DOWN:
        return '2';
      case KeyEvent.VK_LEFT:
        return '4';
      case KeyEvent.VK_RIGHT:
        return '6';
      case KeyEvent.VK_HOME:
        return '7';
      case KeyEvent.VK_END:
        return '1';
      case KeyEvent.VK_PAGE_UP:
        return '9';
      case KeyEvent.VK_PAGE_DOWN:
        return '3';
      case KeyEvent.VK_ESCAPE:
        return 'q';
      default:
        return Character.toLowerCase(e.getKeyChar());
    }
  }

  static int checkbounds(int i,int upperbound){
    if(i<0) return 0;
    return i<upperbound?i:upperbound-1;
  }
}
