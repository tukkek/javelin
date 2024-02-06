package javelin.controller.content.map.terrain.hill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.controller.walker.Walker;
import javelin.controller.walker.pathing.DirectPath;
import javelin.old.RPG;
import javelin.view.Images;

/** Bottle-neck-type terrain surrounded by light vegetation. */
public class Pass extends Map{
  /** Constructor. */
  public Pass(){
    super("Pass",DndMap.SIZE,DndMap.SIZE);
    floor=Images.get(List.of("terrain","forestfloor"));
    obstacle=rock;
    wall=Images.get(List.of("terrain","treeforest"));
  }

  List<Point> grow(List<Point> subarea,int target,Set<Point> area){
    subarea.sort(Comparator.comparing(p->p.x*1000+p.y));
    while(subarea.size()<target){
      var p=subarea.get(RPG.low(0,subarea.size()-1)).clone();
      while(subarea.contains(p)) p.displace();
      if(p.validate(map.length,map[0].length)&&area.contains(p)) subarea.add(p);
    }
    return subarea;
  }

  int seed(){
    var nseeds=2;
    var seeds=new ArrayList<Integer>(nseeds);
    var height=map[0].length;
    for(var i=0;i<nseeds;i++) seeds.add(RPG.r(0,height-1));
    seeds.sort(Comparator.comparingInt(p->Math.abs(height/2-p)));
    return seeds.get(0);
  }

  List<Point> path(Set<Point> maparea,int target){
    var a=new Point(0,seed());
    var b=new Point(map[0].length-1,seed());
    var path=new ArrayList<>(List.of(a,b));
    var walker=new Walker(a,b);
    walker.pathing=new DirectPath();
    path.addAll(walker.walk());
    return grow(path,target,maparea);
  }

  void flood(List<Point> area,int size){
    var seed=new ArrayList<>(List.of(RPG.pick(area)));
    for(var water:grow(seed,size,new HashSet<>(area)))
      putwater(water.x,water.y);
  }

  void obstruct(Set<Point> area){
    var seed=new ArrayList<>(List.of(RPG.pick(area)));
    for(var boulder:grow(seed,RPG.low(4,10),area)){
      var x=boulder.x;
      var y=boulder.y;
      map[x][y].clear();
      putobstacle(x,y);
    }
  }

  @Override
  public void generate(){
    var area=Point.getrange(map.length,map[0].length);
    for(var p:area) if(RPG.chancein(2)) putwall(p.x,p.y);
    var size=area.size();
    var clear=size/2+RPG.randomize(size/6);
    var path=path(area,clear);
    for(var p:path){
      var x=p.x;
      var y=p.y;
      map[x][y].clear();
      if(RPG.chancein(10)) putobstacle(x,y);
    }
    while(RPG.chancein(2)) flood(path,clear/RPG.high(4,20));
    var boulders=RPG.high(4,8);
    for(var i=0;i<boulders;i++) obstruct(area);
    rotate();
  }
}
