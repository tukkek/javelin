package javelin.controller.content.terrain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.content.map.Maps;
import javelin.controller.content.map.terrain.plain.Farm;
import javelin.controller.content.map.terrain.plain.Field;
import javelin.controller.content.map.terrain.plain.Grasslands;
import javelin.controller.content.map.terrain.plain.PlainsShore;
import javelin.controller.content.terrain.hazard.Flood;
import javelin.controller.content.terrain.hazard.Hazard;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.old.RPG;

/**
 * Easiest and most even terrain type.
 *
 * @author alex
 */
public class Plains extends Terrain{
  static final Maps MAPS=new Maps(
      List.of(Farm.class,Grasslands.class,Field.class));

  /** Constructor. */
  public Plains(){
    super(MAPS,new Maps(List.of(PlainsShore.class)));
    name="plains";
    difficultycap=-4;
    speedtrackless=3/4f;
    speedroad=1f;
    speedhighway=1f;
    visionbonus=+2;
    representation=' ';
    safe=true;
  }

  @Override
  protected Point generatesource(World world){
    return RPG.pick(new ArrayList<>(gettiles(world)));
  }

  @Override
  protected HashSet<Point> generatestartingarea(World world){
    return gettiles(world);
  }

  // @Override
  // public boolean generatetown(Point p, World w) {
  // return search(p, DESERT, 1, w) == 0 && super.generatetown(p, w);
  // }

  @Override
  public Set<Hazard> gethazards(boolean special){
    var hazards=super.gethazards(special);
    if(special){
      var location=new Point(Squad.active.x,Squad.active.y);
      if(search(location,WATER,1,World.getseed())>0
          ||search(location,MARSH,1,World.getseed())>0)
        hazards.add(new Flood());
    }
    return hazards;
  }
}
