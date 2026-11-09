package javelin.controller.content.action;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.content.action.ai.attack.AttackResolver;
import javelin.controller.content.action.ai.attack.MeleeAttack;
import javelin.controller.content.fight.Fight;
import javelin.controller.exception.RepeatTurn;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.StatusPanel;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.mappanel.battle.overlay.TargetOverlay;
import javelin.view.mappanel.overlay.Overlay;
import javelin.view.screen.StatisticsScreen;

/// Lets the player examine surroundings and units.
public class Examine extends Action{
  static final HashMap<Character,Point> DIRECTIONS=new HashMap<>();
  static final char SHOW='s';

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

  /** Constructor. */
  public Examine(){
    super("examine",new String[]{"x"});
    allowburrowed=true;
  }

  static Point locate(){
    var cursor=(TargetOverlay)Overlay.get();
    return new Point(cursor.x,cursor.y);
  }

  static void message(String s){
    MessagePanel.get().clear();
    var message="Move the cursor over a combatant, press s to show its character sheet.\n\n";
    message+=s;
    Javelin.message(message,Javelin.Delay.NONE);
    Javelin.redraw();
  }

  static void describe(Square s){
    if(s.flooded) message("Flooded.");
    else if(s.blocked) message("Blocked.");
  }

  static String describe(Combatant target){
    var s=Fight.state;
    var active=BattlePanel.current;
    var text=target.printstatus(s);
    if(active.getlocation().distanceinsteps(target.getlocation())==1){
      var melee=MeleeAttack.INSTANCE;
      var attack=active.source.melee.get(0).get(0);
      var resolver=new AttackResolver(melee,active,target,attack,s);
      resolver.preview(target);
      text+=", "+resolver.chance;
    }
    if(text.isEmpty()) return target.toString();
    return "%s (%s)".formatted(target,text);
  }

  static void inspect(Point p){
    var state=Fight.state;
    var c=state.getcombatant(p.x,p.y);
    if(c==null){
      var square=state.map[p.x][p.y];
      describe(square);
      return;
    }
    message(describe(c));
    StatusPanel.looking=c;
  }

  static void look(Point p){
    if(p==null) return;
    var panel=(BattlePanel)MapPanel.get();
    var tile=panel.tiles[p.x][p.y];
    if(!panel.daylight&&!tile.discovered){
      message("Unseen.");
      return;
    }
    message("");
    StatusPanel.looking=null;
    inspect(p);
    StatusPanel.get().repaint();
  }

  static void set(int x,int y){
    Overlay.set(new TargetOverlay(x,y));
    MapPanel.get().center(x,y,true);
    look(locate());
    StatusPanel.get().repaint();
  }

  static void move(Point delta){
    var point=locate();
    var x=point.x+delta.x;
    var y=point.y+delta.y;
    var map=Fight.state.map;
    x=RPG.clamp(0,x,map.length-1);
    y=RPG.clamp(0,y,map[0].length-1);
    set(x,y);
  }

  static boolean show(char key){
    if(key!=SHOW) return false;
    var target=Fight.state.getcombatant(locate());
    new StatisticsScreen().open(target);
    throw new RepeatTurn();
  }

  static Character press(){
    var e=Javelin.input();
    if(e==null) return null;
    return switch(e.getKeyCode()){
      case KeyEvent.VK_UP -> '8';
      case KeyEvent.VK_DOWN -> '2';
      case KeyEvent.VK_LEFT -> '4';
      case KeyEvent.VK_RIGHT -> '6';
      case KeyEvent.VK_HOME -> '7';
      case KeyEvent.VK_END -> '1';
      case KeyEvent.VK_PAGE_UP -> '9';
      case KeyEvent.VK_PAGE_DOWN -> '3';
      case KeyEvent.VK_ESCAPE -> 'q';
      default -> Character.toLowerCase(e.getKeyChar());
    };
  }

  static Point navigate(){
    var delta=new Point(0,0);
    while(delta!=null){
      var key=press();
      if(key==null||show(key)) continue;
      delta=DIRECTIONS.get(key);
      if(delta!=null) move(delta);
    }
    return locate();
  }

  @Override
  public boolean perform(Combatant active){
    try{
      var point=active.getlocation();
      set(point.x,point.y);
      point=navigate();
      Overlay.clear();
      look(point);
      throw new RepeatTurn();
    }finally{
      StatusPanel.looking=null;
      MessagePanel.active.clear();
    }
  }
}
