package javelin.controller.content.map.terrain.mountain;

import java.util.List;

import javelin.controller.content.map.terrain.forest.ForestPath;
import javelin.controller.content.terrain.Mountains;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * A version of Forest Path but for {@link Mountains}.
 *
 * @author alex
 */
public class MountainPath extends ForestPath{
  /** Constructor. */
  public MountainPath(){
    name="Mountain path";
    wall=Images.get(List.of("terrain","ruggedwall"));
    floor=Images.get(List.of("terrain","desert"));
    obstacle=Images.get(List.of("terrain","rock2"));
    paths=RPG.r(1,4)+4;
    river=RPG.chancein(9);
    riverwidth=new int[]{1,2};
    obstructed=RPG.r(5,20)/100f;
  }
}
