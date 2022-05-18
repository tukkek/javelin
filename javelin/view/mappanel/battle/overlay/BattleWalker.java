package javelin.view.mappanel.battle.overlay;

import java.awt.Color;
import java.util.LinkedList;

import javelin.controller.Point;
import javelin.controller.content.action.Movement;
import javelin.controller.content.fight.Fight;
import javelin.controller.walker.overlay.OverlayStep;
import javelin.controller.walker.overlay.OverlayWalker;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.mappanel.battle.BattleTile;
import javelin.view.screen.BattleScreen;

public class BattleWalker extends OverlayWalker{
  /**
   * Note that AP cost has a different meaning depending on context. For battle
   * is literal AP, for world and dungeons is chance of encounter, unrelated to
   * time.
   *
   * @author alex
   */
  public class BattleStep extends OverlayStep{
    public float apcost;
    public boolean engaged;
    public boolean safe=false;
    public float totalcost;

    public BattleStep(Point p,final float totalcost){
      super(p);
      engaged=isengaged||engage(current,p,state);
      apcost=getcost();
      this.totalcost=totalcost+apcost;
      update();
    }

    void update(){
      color=engaged||totalcost>.5?Color.RED:Color.GREEN;
      text=Float.toString(totalcost).substring(0,3);
    }

    protected float getcost(){
      var c=current;
      return engaged?Movement.disengage(c):BattleWalker.getcost(c,x,y,state);
    }
  }

  final boolean isengaged;
  Combatant current;
  BattleState state;

  public BattleWalker(Point from,Point to,Combatant current,BattleState state){
    super(from,to);
    this.current=current;
    this.state=state;
    isengaged=engage(current,from,state);
  }

  @Override
  protected Point step(Point step,LinkedList<Point> previous){
    var last=previous.isEmpty()?null:(BattleStep)previous.getLast();
    var totalcost=last==null?BattleScreen.partialmove:last.totalcost;
    return new BattleStep(step,totalcost);
  }

  @Override
  public boolean validate(Point p,LinkedList<Point> previous){
    if(!previous.isEmpty()&&((BattleStep)previous.getLast()).engaged)
      return false;
    var step=(BattleStep)p;
    if(block(current,step,state)) return false;
    var istarget=step.equals(to);
    if(isengaged&&(!previous.isEmpty()||!istarget)) return false;
    if(step.totalcost>1||state.getcombatant(step.x,step.y)!=null) return false;
    var m=state.getmeld(step.x,step.y);
    if(m!=null) return istarget&&m.crystalize(state);
    try{
      var panel=(BattlePanel)BattleScreen.active.mappanel;
      var t=(BattleTile)panel.tiles[p.x][p.y];
      return panel.daylight||t.discovered&&!t.shrouded;
    }catch(NullPointerException|ClassCastException e){
      return false;
    }
  }

  public static boolean engage(Combatant c,Point p,BattleState s){
    return !c.burrowed&&s.getopponents(c).stream()
        .filter(o->!o.source.passive&&p.distanceinsteps(o.getlocation())==1)
        .findAny().isPresent();
  }

  @Override
  public Point resetlocation(){ // TODO
    return null;
  }

  @Override
  public LinkedList<Point> walk(){
    /* TODO this is a very unelegant hack */
    var walk=super.walk();
    if(!walk.isEmpty()){
      var last=(BattleStep)walk.getLast();
      if(last.engaged&&!isengaged){
        last.engaged=false;
        last.apcost=last.getcost();
        last.totalcost-=Movement.disengage(current);
        last.totalcost+=last.apcost;
        last.update();
      }
    }
    return walk;
  }

  /**
   * @return {@link Combatant#ap} to move into a valid {@link BattleTile}.
   * @see Movement#disengage(Combatant)
   */
  static public float getcost(Combatant c,int x,int y,BattleState s){
    var source=c.source;
    var m=s.map[x][y];
    float speed;
    if(c.burrowed) speed=source.burrow;
    else if(m.blocked) speed=source.fly;
    else if(m.flooded) speed=Math.max(source.swim(),source.walk/2);
    else speed=Math.max(source.walk,source.fly);
    return Movement.toap(speed);
  }

  /** @return <code>true</code> if unit cannot fly and tile is blocked. */
  public static boolean block(Combatant c,Point to,BattleState s){
    return s.map[to.x][to.y].blocked
        &&(!Fight.current.map.flying||c.source.fly==0);
  }
}
