package javelin.controller.content.terrain;

import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.content.map.Maps;
import javelin.controller.content.map.terrain.hill.RuggedHill;
import javelin.controller.content.map.terrain.mountain.ForbiddingMountain;
import javelin.controller.content.map.terrain.mountain.Meadow;
import javelin.controller.content.map.terrain.mountain.MountainCave;
import javelin.controller.content.map.terrain.mountain.MountainPass;
import javelin.controller.content.map.terrain.mountain.MountainPath;
import javelin.controller.content.map.terrain.mountain.MountainsOfMadness;
import javelin.controller.content.map.terrain.mountain.Ridge;
import javelin.controller.content.terrain.hazard.Break;
import javelin.controller.content.terrain.hazard.Cold;
import javelin.controller.content.terrain.hazard.Hazard;
import javelin.controller.content.terrain.hazard.Rockslide;
import javelin.model.world.Season;
import javelin.model.world.World;

/**
 * High altitude, snowy on winter.
 *
 * @author alex
 */
public class Mountains extends Terrain{
  /** Constructor. */
  public Mountains(){
    name="mountains";
    difficultycap=-2;
    speedtrackless=1/2f;
    speedroad=3/4f;
    speedhighway=3/4f;
    visionbonus=+4;
    representation='M';
    survivalbonus=-2;
  }

  @Override
  public Maps getmaps(){
    var m=new Maps();
    m.addAll(List.of(new Meadow(),new RuggedHill(),new ForbiddingMountain(),
        new MountainPass(),new MountainCave(),new MountainPath(),new Ridge()));
    if(Weather.current==Weather.STORM||Season.current==Season.WINTER)
      m.add(new MountainsOfMadness());
    return m;
  }

  @Override
  protected Point generatesource(World w){
    var source=super.generatesource(w);
    while(!w.map[source.x][source.y].equals(Terrain.FOREST)
        &&search(source,MOUNTAINS,1,w)==0)
      source=super.generatesource(w);
    return source;
  }

  @Override
  public Set<Hazard> gethazards(boolean special){
    var hazards=super.gethazards(special);
    hazards.add(new Cold());
    if(special){
      hazards.add(new Rockslide());
      hazards.add(new Break());
    }
    return hazards;
  }
}
