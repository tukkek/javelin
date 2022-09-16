package javelin.model.world.location.unique;

import javelin.controller.Point;
import javelin.controller.challenge.Tier;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.branch.Branch;

/**
 * A horror-themed {@link Dungeon} minigame. It has a static hub (main floor)
 * with access to 4 areas, one per {@link Tier} - each having one unique
 * {@link Branch} applied to it.
 *
 * @author alex
 */
public class Catacombs extends Dungeon{
  class Entrance extends DungeonEntrance{
    Entrance(Dungeon d){
      super(d);
    }

    @Override
    public String getimagename(){
      return "catacombs";
    }

    @Override
    public void place(Point p){
      //TODO remove
      dungeon.generate();
      super.place(p);
    }
  }

  /** Constructor. */
  public Catacombs(){
    super("Catacombs",1,1);
    images.put(DungeonImages.FLOOR,"floorcatacombs");
    images.put(DungeonImages.WALL,"wallcatacombs");
    entrance=new Entrance(this);
  }

  @Override
  protected synchronized String baptize(String base){
    return base;
  }

  /** @see DungeonEntrance#place(Point) */
  public void place(Point p){
    entrance.place(p);
  }
}
