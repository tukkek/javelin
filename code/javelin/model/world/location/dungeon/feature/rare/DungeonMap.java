package javelin.model.world.location.dungeon.feature.rare;

import java.util.ArrayList;
import java.util.HashSet;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.common.Brazier;
import javelin.old.RPG;
import javelin.view.mappanel.MapPanel;

/**
 * Shows an area of the {@link DungeonFloor} map, with a cumulative 50% chance
 * of doubling that area (50% chance of 2*area, 25% chance of 3*area, etc).
 *
 * TODO allow other Dungeon floors to be shown.
 *
 * @author alex
 */
public class DungeonMap extends Feature{
  /** Constructor. */
  public DungeonMap(DungeonFloor f){
    super("map");
  }

  @Override
  public boolean activate(){
    var d=Dungeon.active;
    var size=d.size;
    var pool=new HashSet<Point>(size*size);
    for(var x=0;x<size;x++) for(var y=0;y<size;y++)
      if(d.map[x][y]!=FloorTile.WALL) pool.add(new Point(x,y));
    for(var f:new ArrayList<>(pool)) pool.addAll(f.getadjacent());
    var target=RPG.pick(new ArrayList<>(pool));
    var points=pool.stream()
        .sorted((a,b)->Double.compare(a.distance(target),b.distance(target)))
        .limit(pool.size()*RPG.r(10,30)/100).toList();
    for(var point:points) Brazier.reveal(point);
    MapPanel.get().center(target.x,target.y,true);
    Javelin.message("A map reveals a portion of the level!",false);
    return true;
  }
}
