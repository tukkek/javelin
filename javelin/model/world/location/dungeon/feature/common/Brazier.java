package javelin.model.world.location.dungeon.feature.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.controller.walker.Walker;
import javelin.controller.walker.pathing.DirectPath;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.branch.temple.FireTemple;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.view.mappanel.dungeon.DungeonTile;

/**
 * Lights up any square on this feature's field of vision, revealing any hidden
 * {@link Feature}s along the way too. Actually cheats a little bit by revealing
 * any square adjacent to a quare in the line of sight, to give it a bit more of
 * a "wow" radiance effect.
 *
 * @see FireTemple
 * @see Feature#discover(javelin.model.unit.Combatant, int)
 * @author alex
 */
public class Brazier extends Feature{
  class VisionWalker extends Walker{
    public VisionWalker(Point from,Point to){
      super(from,to);
      pathing=new DirectPath();
    }

    @Override
    public boolean validate(Point p,LinkedList<Point> previous){
      return Dungeon.active.map[p.x][p.y]!=FloorTile.WALL;
    }
  }

  /** Constructor. */
  public Brazier(DungeonFloor f){
    super("brazier");
  }

  HashSet<Point> crawl(){
    var skip=new HashSet<Point>();
    var see=new HashSet<Point>();
    var s=Dungeon.active.size;
    var seen=true;
    for(var range=1;seen;range++){
      seen=false;
      var steps=new ArrayList<>(
          Point.getrange(x-range,x+range+1,y-range,y+range+1).stream()
              .filter(p->p.validate(s,s)).toList());
      steps.removeAll(skip);
      skip.addAll(steps);
      for(var step:steps){
        var path=new VisionWalker(getlocation(),step).walk();
        if(path==null) continue;
        seen=true;
        see.add(step);
        see.addAll(path);
      }
    }
    return see;
  }

  @Override
  public boolean activate(){
    var revealed=crawl();
    for(var r:new ArrayList<>(revealed))
      if(Dungeon.active.map[r.x][r.y]!=FloorTile.WALL)
        revealed.addAll(r.getadjacent());
    for(var r:revealed) reveal(r);
    Javelin.redraw();
    var p=JavelinApp.context.getsquadlocation();
    JavelinApp.context.view(p.x,p.y);
    Javelin.message("You light up the brazier!",false);
    return true;
  }

  /**
   * @param r Shows {@link DungeonTile} and
   *   {@link Feature#discover(javelin.model.unit.Combatant, int)} any feature
   *   in that tile.i
   */
  static public void reveal(Point r){
    Dungeon.active.setvisible(r.x,r.y);
    var f=Dungeon.active.features.get(r.x,r.y);
    if(f!=null) f.discover(null,9000);
  }
}
