package javelin.controller.content.action;

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
import javelin.view.screen.BattleScreen;

/**
 * @see AiMovement
 * @author alex
 */
public class Movement extends Action{
  /** Cost to disengage. */
  public static final float DISENGAGE=.25f;
  private final String descriptivekeys;
  /** TODO hack */
  public static boolean lastmovewasattack=false;

  /**
   * @see Action#Action(String, String[]).
   * @param descriptivekeys Used in printed description to user.
   */
  public Movement(final String name,final String[] keys,
      final String descriptivekeys){
    super(name,keys);
    this.descriptivekeys=descriptivekeys;
    allowburrowed=true;
  }

  /**
   * @param x Location.
   * @param y Location.
   * @param disengaging
   * @return How much it costs to move to the specified square.
   */
  static public float cost(final Combatant c,final BattleState s,int x,int y,
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
    try{
      var disengaging=Fight.state.isengaged(c);
      final var to=doDirection(this,c);
      if(to==null) return false;
      var meld=Fight.state.getmeld(to.x,to.y);
      if(!Movement.lastmovewasattack){
        var ap=cost(c,Fight.state,to.x,to.y,disengaging);
        BattleScreen.partialmove+=ap;
        c.ap+=ap;
      }
      final var finishmove=meld!=null||disengaging||Movement.lastmovewasattack
          ||BattleScreen.partialmove>=.5f;
      if(!finishmove) throw new RepeatTurn();
      new Audio("move",c).play();
      BattleScreen.partialmove=0;
      if(!Movement.lastmovewasattack) if(meld==null||meld.meldsat>c.ap){
        var action=disengaging?"disengages":"moves";
        Javelin.message(c+" "+action+"...",Javelin.Delay.WAIT);
      }else Fight.current.meld(c,meld);
      return true;
    }finally{
      Movement.lastmovewasattack=false;
    }
  }

  Point doDirection(final ActionDescription action,Combatant hero){
    final var direction=convertActionToDirection(action);
    final var tox=hero.location[0]+direction.x;
    final var toy=hero.location[1]+direction.y;
    return Movement.move(tox,toy,hero)?new Point(tox,toy):null;
  }

  static Point convertActionToDirection(final ActionDescription action){
    final var direction=new Point(Integer.MIN_VALUE,Integer.MIN_VALUE);
    if(action==Action.MOVE_N){
      direction.x=0;
      direction.y=-1;
    }
    if(action==Action.MOVE_S){
      direction.x=0;
      direction.y=1;
    }
    if(action==Action.MOVE_W){
      direction.x=-1;
      direction.y=0;
    }
    if(action==Action.MOVE_E){
      direction.x=1;
      direction.y=0;
    }
    if(action==Action.MOVE_NW){
      direction.x=-1;
      direction.y=-1;
    }
    if(action==Action.MOVE_NE){
      direction.x=1;
      direction.y=-1;
    }
    if(action==Action.MOVE_SW){
      direction.x=-1;
      direction.y=1;
    }
    if(action==Action.MOVE_SE){
      direction.x=1;
      direction.y=1;
    }
    return direction.x==Integer.MIN_VALUE?null:direction;
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
    lastmovewasattack=true;
    active.meleeattacks(target,s);
    return true;
  }
}
