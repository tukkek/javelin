package javelin.controller.content.action.world;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.content.terrain.hazard.Hazard;
import javelin.controller.exception.RepeatTurn;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

/**
 * Makes a movement on the overworld or {@link DungeonFloor}.
 *
 * TODO {@link WorldScreen} hierarchy should be refactored into proper Battle /
 * Dungeon / World screens.
 *
 * TODO {@link #perform(WorldScreen)} needs refactoring after 2.0
 *
 * @see Javelin#getperiod()
 * @author alex
 */
public class WorldMove extends WorldAction{
  /**
   * Represents time spent on resting, eating, sleeping, etc.
   *
   * TODO forced march
   */
  public static final float NORMALMARCH=4f/5f;
  /**
   * Ideally a move should always be 6 hours in a worst-case scenario (the time
   * of a period), so as to avoid a move taking longer than a period, which
   * could confuse {@link Hazard}s.
   *
   * @see #TIMECOST
   */
  public static final float MOVETARGET=6f;
  /**
   * How much time it takes to walk a single square with speed 30 (~30mph,
   * normal human speed).
   *
   * Calculation of worst case scenario (which should take 6 hours): 6 * 15/30ft
   * (slow races) * .5 (bad terrain)
   *
   * @see Monster#gettopspeed()
   * @see #MOVETARGET
   * @see #NORMALMARCH
   */
  public static final float TIMECOST=NORMALMARCH*(MOVETARGET*15f/30f)/2f;
  /**
   * Used when {@link WorldScreen#react(int, int)} returns <code>true</code> but
   * we still want to stop moving.
   */
  public static boolean abort;

  /** Horizontal direction. */
  public final int deltax;
  /** Vertical direction. */
  public final int deltay;

  /**
   * Constructor
   *
   * @param keycodes Integer/char keys.
   * @param deltax Direction of x movement.
   * @param deltay Direction of y movement.
   * @param keys Text keys.
   */
  public WorldMove(final int[] keycodes,final int deltax,final int deltay,
      final String[] keys){
    super("Move ("+keys[1].charAt(0)+")",keycodes,keys);
    this.deltax=deltax;
    this.deltay=deltay;
  }

  @Override
  public void perform(WorldScreen s){
    var p=JavelinApp.context.getsquadlocation();
    var x=p.x+deltax;
    var y=p.y+deltay;
    move(x,y);
  }

  /**
   * @return <code>true</code> to continue moving, if relevant.
   * @see BattleScreen
   */
  public static boolean move(int x,int y){
    var c=JavelinApp.context;
    if(!c.validatepoint(x,y)) throw new RepeatTurn();
    abort=false;
    if(c.react(x,y)||abort||!place(x,y)) return false;
    try{
      var stop=walk()&&!c.explore(x,y);
      heal();//TASK
      return !stop;
    }finally{//battle starts?
      var s=Squad.active;
      var t=Terrain.get(x,y);
      if(s!=null&&t.enter(s,x,y)) s.move(true,t,x,y);
    }
  }

  /// @return <code>true</code> if moved current actor to the given location.
  public static boolean place(int x,int y){
    var c=JavelinApp.context;
    if(!c.allowmove(x,y)||!c.validatepoint(x,y)) return false;
    c.updatelocation(x,y);
    c.view(x,y);
    return true;
  }

  static void heal(){
    for(final Combatant c:Squad.active.members)
      if(c.source.fasthealing!=0) c.heal(c.maxhp,false);
  }

  static boolean walk(){
    if(Dungeon.active!=null) return true;
    var point=JavelinApp.context.getsquadlocation();
    var squads=World.getall(Squad.class).stream()
        .filter(s->s.getlocation().equals(point)).map(s->(Squad)s).toList();
    if(squads.size()<=1) return true;
    squads.get(0).join(squads.get(1));
    return false;
  }
}
