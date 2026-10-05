package javelin.controller.content.map.terrain.underground;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.controller.content.terrain.Underground;
import javelin.controller.walker.Walker;
import javelin.controller.walker.pathing.DirectPath;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * Represents an {@link Underground} tunnel made for mining.
 *
 * @author alex
 */
public class Railway extends Map{
  static final int SIZE=DndMap.SIZE;
  static final int CARVED=SIZE*SIZE/3;

  /** Constructor. */
  public Railway(){
    super("Underground railway",SIZE,SIZE);
    flooded=Images.get(List.of("terrain","aquatic"));
    obstacle=rock;
  }

  List<Point> walk(Point from,Point to){
    var w=new Walker(from,to);
    w.pathing=new DirectPath();
    return w.walk();
  }

  void carve(){
    var rail=new HashSet<Point>(CARVED);
    var connections=new ArrayList<Point>();
    connections.add(new Point(RPG.r(SIZE),RPG.r(SIZE)));
    while(rail.size()<CARVED){
      var from=RPG.pick(connections);
      var to=new Point(RPG.r(SIZE),RPG.r(SIZE));
      connections.add(to);
      for(var p:walk(from,to)){
        rail.add(p);
        rail.addAll(p.getadjacent(RPG.low(0,2)));
      }
    }
    for(var r:rail) if(r.validate(0,0,SIZE,SIZE)) map[r.x][r.y].blocked=false;
  }

  void flood(){
    if(!RPG.chancein(4)) return;
    var from=new Point(RPG.r(SIZE),0);
    var to=new Point(RPG.r(SIZE),RPG.r(SIZE));
    var extend=RPG.r(1,3);
    if(extend==1) to.x=0;
    else if(extend==2) to.x=SIZE-1;
    else to.y=SIZE-1;
    var river=new HashSet<Point>();
    for(var w:walk(from,to)){
      river.add(w);
      river.addAll(w.getadjacent(RPG.low(1,2)));
    }
    for(var r:river) if(r.validate(0,0,SIZE,SIZE)){
      var s=map[r.x][r.y];
      if(!s.blocked) s.flooded=true;
    }
  }

  void decorate(){
    //    var rocks=new HashSet<>();
    for(var p:Point.getrange(0,0,SIZE,SIZE)) if(p.getadjacent().stream()
        .filter(a->a.validate(0,0,SIZE,SIZE)&&map[a.x][a.y].blocked).findAny()
        .isPresent()){
          if(RPG.chancein(2)) map[p.x][p.y].obstructed=true;
          for(var a:p.getadjacent())
            if(a.validate(0,0,SIZE,SIZE)&&RPG.chancein(10))
              map[a.x][a.y].obstructed=true;
        }
  }

  @Override
  public void generate(){
    for(var p:Point.getrange(0,0,SIZE,SIZE)) map[p.x][p.y].blocked=true;
    carve();
    decorate();
    flood();
    rotate();
  }
}
