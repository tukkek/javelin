package javelin.controller.content.action;

import java.awt.event.KeyEvent;

import javelin.Javelin;
import javelin.controller.Audio;
import javelin.controller.Point;
import javelin.controller.content.action.ai.AiMovement;
import javelin.controller.content.fight.Fight;
import javelin.controller.exception.RepeatTurn;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.skill.Acrobatics;
import javelin.model.unit.skill.Skill;
import javelin.view.mappanel.battle.BattleMouse;
import javelin.view.mappanel.battle.overlay.BattleWalker;

/**
 * @see AiMovement
 * @author alex
 */
public class Movement extends Action{
  /// Cost to disengage in action-points
  public static final float DISENGAGE=.25f;
  /// North-west
  public static final Movement NW=new Movement(Direction.NW,
      new String[]{"7","KeyEvent"+KeyEvent.VK_HOME,"U"},
      "↖ (7 on numpad, or U)");
  /// North
  public static final Movement N=new Movement(Direction.N,
      new String[]{"8","KeyEvent"+KeyEvent.VK_UP,"I"},"↑ (8 on numpad, or I)");
  /// North-east
  public static final Movement NE=new Movement(Direction.NE,
      new String[]{"9","KeyEvent"+KeyEvent.VK_PAGE_UP,"O"},
      "↗ (9 on numpad, or O)");
  /// West
  public static final Movement W=new Movement(Direction.W,
      new String[]{"4","KeyEvent"+KeyEvent.VK_LEFT,"J","←"},
      "← (4 on numpad, or J)");
  /// East
  public static final Movement E=new Movement(Direction.E,
      new String[]{"6","KeyEvent"+KeyEvent.VK_RIGHT,"L","→"},
      "→ (6 on numpad, or L)");
  /// South-west
  public static final Movement SW=new Movement(Direction.SW,
      new String[]{"1","KeyEvent"+KeyEvent.VK_END,"M"},"↙ (1 in numpad, or M)");
  /// South
  public static final Movement S=new Movement(Direction.S,
      new String[]{"2","KeyEvent"+KeyEvent.VK_DOWN,"<"},
      "↓ (2 in numpad, or <)");
  /// South-east
  public static final Movement SE=new Movement(Direction.SE,
      new String[]{"3","KeyEvent"+KeyEvent.VK_PAGE_DOWN,">"},
      "↘ (3 on numpad, or >)");

  /**
   * Keeps track of human {@link Movement} steps using the keyboard. We want to
   * allow them to move up to a {@link ActionCost#MOVE} action without
   * disruption their current action - even if {@link RepeatTurn} is thrown from
   * another action (like cancelling a menu, etc.).
   *
   * {@link BattleMouse} and {@link BattleWalker} take this into consideration
   * too, so as not to allow the player to cheat by using half their share of
   * keyboard movements and then using another {@link ActionCost#MOVE} worth of
   * movement witht the mouse. Mouse moves, however are instantaneous and apply
   * the relevant AP cost, plus the current value of this variable. This of
   * course still allows player to make shorter, tactical moves (consuming less
   * AP) if so desired.
   */
  static public float partial=0;

  static boolean attacked=false;

  String descriptivekeys;
  Direction direction;

  /// Constructor
  Movement(Direction d,String[] keys,String descriptivekeys){
    super("Move (%s)".formatted(d.arrow),keys);
    direction=d;
    this.descriptivekeys=descriptivekeys;
    allowburrowed=true;
  }

  /**
   * @param x Location.
   * @param y Location.
   * @return How much it costs to move to the specified square.
   */
  static public float cost(Combatant c,BattleState s,int x,int y,
      boolean disengaging){
    float speed;
    if(c.burrowed) speed=c.source.burrow;
    else if(disengaging) return disengage(c);
    else{
      speed=c.gettopspeed(s);
      if(s.map[x][y].flooded&&c.source.fly==0)
        speed=c.source.swim()>0?c.source.swim:speed/2f;
    }
    return toap(speed);
  }

  /**
   * @param speed Speed in feet (5 feet = 1 square).
   * @return Cost of 1 square of movement in action points.
   */
  public static float toap(float speed){
    return .5f/(speed/5f);
  }

  /**
   * @param c Checks {@link Acrobatics}.
   * @return Action points to disengage.
   */
  static public float disengage(Combatant c){
    return Math.max(.01f,DISENGAGE-.01f*Skill.ACROBATICS.getbonus(c));
  }

  @Override
  public String[] getDescriptiveKeys(){
    return new String[]{descriptivekeys};
  }

  @Override
  public boolean perform(Combatant c){
    // premove
    Movement.attacked=false;
    var s=Fight.state;
    var disengaging=s.isengaged(c);
    // move?
    var to=step(c);
    if(to==null) return false;
    // pay
    var attacked=Movement.attacked;
    if(!attacked){
      var ap=cost(c,s,to.x,to.y,disengaging);
      Movement.partial+=ap;
      c.ap+=ap;
    }
    /// end?
    var audio="move";
    var meld=s.getmeld(to.x,to.y);
    String message=null;
    if(Movement.partial>=.5f) message="%s moves...".formatted(c);
    else if(attacked) audio=null;
    else if(disengaging) message="%s disengages...".formatted(c);
    else if(meld!=null&&meld.meldsat<=c.ap) Fight.current.meld(c,meld);
    else throw new RepeatTurn();
    Movement.partial=0;
    if(audio!=null) new Audio(audio,c).play();
    if(message!=null) Javelin.message(message,Javelin.Delay.WAIT);
    return true;
  }

  Point step(Combatant hero){
    var point=hero.getlocation();
    var delta=direction.point;
    point.x+=delta.x;
    point.y+=delta.y;
    return Movement.move(point.x,point.y,hero)?point:null;
  }

  /// @return <code>true</code> if some action taken
  static public boolean move(int x,int y,Combatant active){
    var s=Fight.state;
    var map=s.map;
    var valid=0<=x&&x<map.length;
    if(!valid) throw new RepeatTurn();
    valid=0<=y&&y<map[x].length;
    if(!valid) throw new RepeatTurn();
    var target=s.getcombatant(x,y);
    var blocked=map[x][y].blocked;
    var fly=active.source.fly>0&&Fight.current.map.flying;
    if(target==null&&blocked&&!fly) return false;
    var m=s.getmeld(x,y);
    if(m!=null&&!m.crystalize(s)) throw new RepeatTurn();
    if(target==null){
      active.location[0]=x;
      active.location[1]=y;
      return true;
    }
    if(target.isally(active,s)) throw new RepeatTurn();
    if(target.burrowed) Dig.refuse();
    attacked=true;
    active.meleeattacks(target,s);
    return true;
  }
}
