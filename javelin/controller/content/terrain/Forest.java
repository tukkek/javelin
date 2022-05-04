package javelin.controller.content.terrain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.content.map.Maps;
import javelin.controller.content.map.terrain.forest.DenseForest;
import javelin.controller.content.map.terrain.forest.ForestPath;
import javelin.controller.content.map.terrain.forest.MediumForest;
import javelin.controller.content.map.terrain.forest.SparseForest;
import javelin.controller.content.terrain.hazard.Break;
import javelin.controller.content.terrain.hazard.FallingTrees;
import javelin.controller.content.terrain.hazard.GettingLost;
import javelin.controller.content.terrain.hazard.Hazard;
import javelin.model.world.World;

/**
 * Dense forest but not quite a jungle.
 *
 * @author alex
 */
public class Forest extends Terrain{
  static final Maps MAPS=new Maps(List.of(SparseForest.class,MediumForest.class,
      DenseForest.class,ForestPath.class));

  /** Constructor. */
  public Forest(){
    super(MAPS,Maps.EMPTY);
    name="forest";
    difficultycap=-3;
    speedtrackless=1/2f;
    speedroad=1f;
    speedhighway=1f;
    visionbonus=-4;
    representation='F';
    survivalbonus=+4;
  }

  @Override
  HashSet<Point> generatearea(World world){
    return gettiles(world);
  }

  @Override
  public Set<Hazard> gethazards(boolean special){
    var hazards=super.gethazards(special);
    hazards.add(new GettingLost(16));
    if(special){
      hazards.add(new FallingTrees());
      hazards.add(new Break());
    }
    return hazards;
  }
}
