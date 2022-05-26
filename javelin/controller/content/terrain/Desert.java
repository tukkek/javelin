package javelin.controller.content.terrain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.content.map.Maps;
import javelin.controller.content.map.terrain.desert.DesertShore;
import javelin.controller.content.map.terrain.desert.Rocks;
import javelin.controller.content.map.terrain.desert.RockyDesert;
import javelin.controller.content.map.terrain.desert.Ruins;
import javelin.controller.content.map.terrain.desert.SandyDesert;
import javelin.controller.content.map.terrain.desert.Tundra;
import javelin.controller.content.terrain.hazard.Cold;
import javelin.controller.content.terrain.hazard.Dehydration;
import javelin.controller.content.terrain.hazard.GettingLost;
import javelin.controller.content.terrain.hazard.Hazard;
import javelin.controller.content.terrain.hazard.Heat;
import javelin.model.world.World;

/**
 * Sandy desert, becomes {@link Tundra} in winter.
 *
 * @author alex
 */
public class Desert extends Terrain{
  static final Maps MAPS=new Maps(List.of(Tundra.class,RockyDesert.class,
      SandyDesert.class,Rocks.class,Ruins.class));
  /**
   * Used instead of normal storms on the desert, makes it easier to get lost.
   *
   * @see #describeweather()
   */
  public static final String SANDSTORM="sandstorm";

  /** Constructor. */
  public Desert(){
    super("desert",MAPS,new Maps(List.of(DesertShore.class)));
    difficultycap=-2;
    movement=1/2f;
    visionbonus=0;
    representation='d';
    liquid=true;
    survivalbonus=-4;
  }

  @Override
  protected Point generatesource(World w){
    var source=super.generatesource(w);
    while(!w.map[source.x][source.y].equals(Terrain.FOREST)
        ||search(source,Terrain.MOUNTAINS,1,w)==0)
      source=super.generatesource(w);
    return source;
  }

  @Override
  public void generatesurroundings(HashSet<Point> area,World w){
    var radius=2;
    for(Point p:area)
      for(var x=-radius;x<=+radius;x++) for(var y=-radius;y<=+radius;y++){
        var surroundingx=p.x+x;
        var surroundingy=p.y+y;
        if(!World.validatecoordinate(surroundingx,surroundingy)) continue;
        if(w.map[surroundingx][surroundingy].equals(Terrain.FOREST))
          w.map[surroundingx][surroundingy]=Terrain.PLAIN;
      }
  }

  @Override
  public Set<Hazard> gethazards(boolean special){
    var hazards=super.gethazards(special);
    hazards.add(new Dehydration());
    hazards.add(new Heat());
    hazards.add(new Cold());
    hazards.add(new GettingLost(describeweather()==SANDSTORM?24:14));
    return hazards;
  }

  @Override
  public String describeweather(){
    return Weather.current==Weather.STORM?SANDSTORM:"";
  }
}
