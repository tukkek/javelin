package javelin.controller.content.terrain;

import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.content.map.Maps;
import javelin.controller.content.map.terrain.hill.GentleHill;
import javelin.controller.content.map.terrain.hill.HillShore;
import javelin.controller.content.map.terrain.hill.RuggedHill;
import javelin.controller.content.terrain.hazard.Hazard;
import javelin.controller.content.terrain.hazard.Rockslide;
import javelin.model.world.World;

/**
 * Similar to {@link Plains}.
 *
 * @author alex
 */
public class Hill extends Terrain{
  /** Constructor. */
  public Hill(){
    name="hill";
    difficultycap=-4;
    speedtrackless=1/2f;
    speedroad=3/4f;
    speedhighway=1f;
    visionbonus=+2;
    representation='^';
    safe=true;
  }

  @Override
  public Maps getmaps(){
    if(checkshore(List.of(Terrain.WATER)))
      return new Maps(List.of(new HillShore()));
    return new Maps(List.of(new GentleHill(),new RuggedHill()));
  }

  @Override
  protected Point generatesource(World w){
    var source=super.generatesource(w);
    while(!w.map[source.x][source.y].equals(Terrain.FOREST)
        ||search(source,Terrain.MOUNTAINS,1,w)==0
            &&search(source,Terrain.PLAIN,1,w)==0)
      source=super.generatesource(w);
    return source;
  }

  @Override
  public Set<Hazard> gethazards(boolean special){
    var hazards=super.gethazards(special);
    if(special) hazards.add(new Rockslide());
    return hazards;
  }
}
