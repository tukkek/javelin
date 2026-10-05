package javelin.controller.content.map.terrain.marsh;

import java.util.List;

import javelin.controller.Point;
import javelin.controller.content.map.terrain.forest.ForestRuin;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.content.terrain.Water;
import javelin.view.Images;

/**
 * More like {@link Water} Ruin but it's fine as it's close to shore.
 *
 * @see Terrain#shoremaps
 */
public class MarshRuin extends ForestRuin{
  /** Constructor. */
  public MarshRuin(){
    super("Water ruin");
    flooded=Images.get(List.of("terrain","aquatic"));
    growths+=3;
  }

  @Override
  public void generate(){
    super.generate();
    for(var p:Point.getrange(width,height)){
      var tile=map[p.x][p.y];
      if(tile.blocked){
        tile.clear();
        tile.flooded=true;
      }
    }
  }
}
