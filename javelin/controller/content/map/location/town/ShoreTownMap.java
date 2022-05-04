package javelin.controller.content.map.location.town;

import static java.util.stream.Collectors.toList;

import java.util.HashSet;
import java.util.List;

import javelin.Debug;
import javelin.controller.Point;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Section;
import javelin.controller.content.map.Section.Sections;
import javelin.controller.content.map.terrain.plain.PlainsShore;
import javelin.controller.exception.GaveUp;
import javelin.model.item.Tier;
import javelin.old.RPG;
import javelin.view.Images;

/** @author alex */
public class ShoreTownMap extends PlainsShore{
  Tier tier;

  /** {@link Debug} constructor. */
  public ShoreTownMap(Tier t){
    tier=t;
    floor=Images.get(List.of("terrain","towngrass"));
    wall=Images.get(List.of("terrain","shipfloor"));
    obstacle=Images.get(List.of("terrain","bush"));
    obstaclechance=8;
  }

  boolean drawtown(){
    var walls=new HashSet<Point>();
    try{
      var t=tier.getordinal()+1;
      var dry=Point.getrange(0,0,DndMap.SIZE,DndMap.SIZE).stream()
          .filter(p->!map[p.x][p.y].flooded).collect(toList());
      var nsections=t+1;
      nsections=RPG.high(nsections,nsections*2);
      var sections=new Sections(Section.class,this);
      sections.segment(nsections,t/4.0,new HashSet<>(dry));
      for(var s:sections) walls.addAll(s.segment(this,Section.class));
    }catch(GaveUp e){
      return false;
    }
    for(var w:walls){
      var m=map[w.x][w.y];
      m.blocked=true;
      m.obstructed=false;
    }
    return true;
  }

  @Override
  public void generate(){
    super.generate();
    while(!drawtown()){
      //continue
    }
  }
}
