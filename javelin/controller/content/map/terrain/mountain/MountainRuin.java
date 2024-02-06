package javelin.controller.content.map.terrain.mountain;

import java.util.List;

import javelin.controller.content.map.terrain.forest.ForestRuin;
import javelin.old.RPG;
import javelin.view.Images;

/** @see ForestRuin */
public class MountainRuin extends ForestRuin{
  /** Constructor. */
  public MountainRuin(){
    super("Mountain ruin");
    wall=Images.get(List.of("terrain","ruggedwall"));
    obstacle=Images.get(List.of("terrain","bush"));
    plants=RPG.r(15,20)/10.0;
  }
}
