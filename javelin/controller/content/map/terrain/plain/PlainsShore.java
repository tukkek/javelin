package javelin.controller.content.map.terrain.plain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.controller.walker.Walker;
import javelin.controller.walker.pathing.DirectPath;
import javelin.old.RPG;
import javelin.view.Images;

/** @author alex */
public class PlainsShore extends Map{
  /** Constructor. */
  public PlainsShore(){
    super("Plains shore",DndMap.SIZE,DndMap.SIZE);
    floor=Images.get(List.of("terrain","grass"));
    obstacle=rock;
    flooded=Images.get(List.of("terrain","aquatic"));
  }

  HashSet<Point> drawshore(){
    HashSet<Point> shore;
    Point tide;
    var border=RPG.r(1,4);
    if(border==1){
      shore=Point.getrange(0,0,DndMap.SIZE,1);
      tide=new Point(0,+1);
    }else if(border==2){
      shore=Point.getrange(DndMap.SIZE-1,0,DndMap.SIZE,DndMap.SIZE);
      tide=new Point(-1,0);
    }else if(border==3){
      shore=Point.getrange(0,DndMap.SIZE-1,DndMap.SIZE,DndMap.SIZE);
      tide=new Point(0,-1);
    }else{
      shore=Point.getrange(0,0,1,DndMap.SIZE);
      tide=new Point(+1,0);
    }
    for(var s:new ArrayList<>(shore)){
      s=new Point(s);
      var size=RPG.high(2,4);
      for(var i=0;i<size;i++){
        shore.add(new Point(s));
        s.x+=tide.x;
        s.y+=tide.y;
      }
    }
    for(var s:shore) map[s.x][s.y].flooded=true;
    return shore;
  }

  void drawriver(HashSet<Point> shore){
    var shape=new ArrayList<Point>(5*10);
    while(shape.size()<6){
      shape.clear();
      var p=RPG.pick(shore);
      while(p.validate(0,0,DndMap.SIZE,DndMap.SIZE)){
        shape.add(p);
        while(shore.contains(p)||shape.contains(p))
          p=RPG.pick(p.getadjacent(2*DndMap.SIZE/5));
      }
      p.bind(0,0,DndMap.SIZE,DndMap.SIZE);
      shape.add(p);
    }
    var river=new HashSet<>(shape);
    for(var i=0;i<shape.size()-1;i+=1){
      var from=shape.get(i);
      var to=shape.get(i+1);
      var walker=new Walker(from,to);
      walker.pathing=new DirectPath();
      river.addAll(walker.walk());
    }
    for(var r:new ArrayList<>(river))
      for(var a:r.getadjacent()) if(!RPG.chancein(3)) river.add(a);
    for(var r:river)
      if(r.validate(0,0,DndMap.SIZE,DndMap.SIZE)) map[r.x][r.y].flooded=true;
  }

  @Override
  public void generate(){
    var s=drawshore();
    drawriver(s);
    for(var x=0;x<DndMap.SIZE;x+=1) for(var y=0;y<DndMap.SIZE;y+=1)
      if(RPG.chancein(30)) map[x][y].obstructed=true;
  }
}
