package javelin.model.world.location;

import java.util.List;

import javelin.model.unit.Combatant;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.view.screen.WorldScreen;

/**
 * An outpost maintains vision of an area around it.
 *
 * @see World#discovered
 * @author alex
 */
public class Outpost extends Fortification{
  /** How many squares away to help vision with. */
  public static final int VISIONRANGE=3;
  private static final String DESCRIPTION="Outpost";

  /** Constructor. */
  public Outpost(){
    super(DESCRIPTION,DESCRIPTION,1,5);
    gossip=true;
    vision=VISIONRANGE;
  }

  /** Puts a new instance in the {@link World} map. */
  public static void build(){
    new Outpost().place();
  }

  @Override
  public boolean interact(){
    super.interact();
    return false;
  }

  /**
   * Given a coordinate shows a big amount of land around that.
   *
   * @param range How far squares away will become visible.
   * @see WorldScreen#discovered
   */
  static public void discover(int xp,int yp,int range){
    for(var x=xp-range;x<=xp+range;x++)
      for(var y=yp-range;y<=yp+range;y++) WorldScreen.discover(x,y);
  }

  @Override
  protected boolean validateplacement(boolean water,World w,List<Actor> actors){
    var o=findnearest(Outpost.class);
    if(o!=null&&o.distance(x,y)<=VISIONRANGE*2) return false;
    return super.validateplacement(water,w,actors);
  }

  @Override
  public List<Combatant> getcombatants(){
    return garrison;
  }
}
