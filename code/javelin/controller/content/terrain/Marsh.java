package javelin.controller.content.terrain;

import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.content.map.Maps;
import javelin.controller.content.map.terrain.marsh.MarshRuin;
import javelin.controller.content.map.terrain.marsh.MarshShore;
import javelin.controller.content.map.terrain.marsh.Moor;
import javelin.controller.content.map.terrain.marsh.Swamp;
import javelin.controller.content.terrain.hazard.Cold;
import javelin.controller.content.terrain.hazard.Flood;
import javelin.controller.content.terrain.hazard.GettingLost;
import javelin.controller.content.terrain.hazard.Hazard;
import javelin.model.world.World;

/**
 * Bog, swamp.
 *
 * @author alex
 */
public class Marsh extends Terrain{
  static final Maps MAPS=new Maps(List.of(Moor.class,Swamp.class));

  /** Constructor. */
  public Marsh(){
    super("marsh",MAPS,new Maps(List.of(MarshShore.class,MarshRuin.class)));
    description="marshlands";
    difficultycap=-1;
    movement=1/2f;
    visionbonus=-2;
    representation='m';
    liquid=true;
    survivalbonus=-2;
  }

  @Override
  protected Point generatesource(World w){
    var source=super.generatesource(w);
    while(!w.map[source.x][source.y].equals(Terrain.FOREST)
        &&search(source,WATER,1,w)==0)
      source=super.generatesource(w);
    return source;
  }

  @Override
  public Set<Hazard> gethazards(boolean special){
    var hazards=super.gethazards(special);
    hazards.add(new GettingLost(10));
    hazards.add(new Cold());
    if(special) hazards.add(new Flood());
    return hazards;
  }
}
