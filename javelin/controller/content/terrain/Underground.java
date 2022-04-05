package javelin.controller.content.terrain;

import java.util.List;

import javelin.controller.Weather;
import javelin.controller.content.map.Maps;
import javelin.controller.content.map.terrain.underground.AncientCave;
import javelin.controller.content.map.terrain.underground.Constructed;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.Wilderness;
import javelin.old.underground.BigCave;
import javelin.old.underground.Caves;
import javelin.old.underground.Complex;
import javelin.old.underground.FloorPlan;
import javelin.old.underground.Maze;
import javelin.old.underground.Pit;
import javelin.view.Images;

/**
 * See {@link Terrain#UNDERGROUND}.
 *
 * @author alex
 */
public class Underground extends Terrain{
  /** Constructor. */
  public Underground(){
    name="underground";
    survivalbonus=-2;
    safe=null;
  }

  @Override
  public Maps getmaps(){
    var maps=new Maps();
    for(var m:List.of(new Caves(),new BigCave(),new Maze(),new Pit(),
        new FloorPlan(),new Complex(),new Constructed(),new AncientCave())){
      var d=Dungeon.active;
      if(d!=null){
        var i=d.dungeon.images;
        if(d.dungeon instanceof Wilderness){
          m.floor=Images.get(i.get(DungeonImages.FLOOR));
          m.wall=Images.get(i.get(DungeonImages.WALL));
        }else{
          m.floor=Images.get(List.of("dungeon",i.get(DungeonImages.FLOOR)));
          m.wall=Images.get(List.of("dungeon",i.get(DungeonImages.WALL)));
        }
        if(m.wall==m.obstacle)
          m.obstacle=Images.get(List.of("terrain","rock2"));
      }
      maps.add(m);
    }
    return maps;
  }

  @Override
  public Integer getweather(){
    return Math.max(Weather.CLEAR,Weather.current-1);
  }
}
