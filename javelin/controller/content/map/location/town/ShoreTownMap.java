package javelin.controller.content.map.location.town;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Debug;
import javelin.controller.Point;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.terrain.plain.PlainsShore;
import javelin.controller.exception.GaveUp;
import javelin.model.item.Tier;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.Images;

/** @author alex */
public class ShoreTownMap extends PlainsShore{
  class Section{
    HashSet<Point> area=new HashSet<>();
    HashSet<Point> structures=new HashSet<>();

    public Section(Point p){
      area.add(p);
    }

    public boolean grow(){
      for(var p:RPG.shuffle(new ArrayList<>(area))){
        var adjacent=p.getadjacent().stream()
            .filter(next->next.validate(0,0,DndMap.SIZE,DndMap.SIZE)
                &&!map[next.x][next.y].flooded&&!area.contains(next))
            .collect(Collectors.toList());
        if(!adjacent.isEmpty()){
          area.addAll(adjacent);
          return true;
        }
      }
      return false;
    }

    public HashSet<Point> draw() throws GaveUp{
      for(var s:segment(RPG.r(1,8),new ArrayList<>(area),.4))
        structures.addAll(s.area);
      return structures;
    }
  }

  Tier tier;

  /** {@link Debug} constructor. */
  public ShoreTownMap(Tier t){
    tier=t;
    floor=Images.get(List.of("terrain","towngrass"));
    wall=Images.get(List.of("terrain","shipfloor"));
    obstacle=Images.get(List.of("terrain","bush"));
    obstaclechance=8;
  }

  /** Constructor. */
  public ShoreTownMap(Town t){
    this(Tier.get(t.population));
  }

  boolean grow(ArrayList<Section> sections){
    for(var s:RPG.shuffle(sections)) if(s.grow()) return true;
    return false;
  }

  List<Section> segment(int nsections,List<Point> area,double occupy)
      throws GaveUp{
    var sections=new ArrayList<Section>(nsections);
    if(area.size()<nsections) throw new GaveUp();
    for(var d:RPG.shuffle(area).subList(0,nsections))
      sections.add(new Section(d));
    var target=area.size()*occupy;
    while(sections.stream().mapToInt(s->s.area.size()).sum()<target)
      if(!grow(sections)) throw new GaveUp();
    return sections;
  }

  boolean drawtown(){
    var walls=new HashSet<Point>();
    try{
      var t=tier.getordinal()+1;
      var dry=Point.getrange(0,0,DndMap.SIZE,DndMap.SIZE).stream()
          .filter(p->!map[p.x][p.y].flooded).collect(toList());
      var nsections=t+1;
      nsections=RPG.high(nsections,nsections*2);
      var sections=segment(nsections,dry,t/4.0);
      for(var s:sections) walls.addAll(s.draw());
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
