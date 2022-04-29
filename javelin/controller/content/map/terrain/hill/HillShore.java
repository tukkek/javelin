package javelin.controller.content.map.terrain.hill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.controller.content.terrain.Hill;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * @see Hill
 * @author alex
 */
public class HillShore extends Map{
  static final int SIZE=DndMap.SIZE;

  Set<Point> walls;

  /** Constructor. */
  public HillShore(){
    super("Hill shore",SIZE,SIZE);
    floor=Images.get(List.of("terrain","floorsand"));
    flooded=Images.get(List.of("terrain","aquatic"));
    wall=Images.get(List.of("terrain","marblewall"));
    obstacle=Images.get(List.of("terrain","bush2"));
  }

  void drawsea(){
    var width=RPG.high(1,4);
    var sea=Point.getrange(SIZE-width,0,SIZE,SIZE);
    for(var i=0;i<RPG.high(1,10);i++){
      var crest=new Point(RPG.pick(sea));
      crest.x-=RPG.high(1,8);
      for(var x=crest.x;x<SIZE-width;x++){
        var step=x-crest.x;
        for(var y=crest.y-step*2;y<=crest.y+step*2;y++){
          var p=new Point(x,y);
          if(p.validate(0,0,SIZE,SIZE)) sea.add(p);
        }
      }
    }
    for(var s:sea) map[s.x][s.y].flooded=true;
  }

  List<Point> seed(int nheads){
    var seeds=new ArrayList<Point>(nheads);
    var limit=RPG.r(SIZE/2,SIZE*3/4);
    while(seeds.size()<nheads) seeds.add(new Point(RPG.r(0,limit),RPG.r(SIZE)));
    return seeds;
  }

  Set<Point> drawwalls(){
    var nheads=40;
    var seeds=seed(nheads);
    var walls=new HashSet<>(seeds);
    while(walls.size()<SIZE*SIZE/8){
      var h=RPG.pick(seeds);
      var next=new Point(h);
      next.displaceaxis();
      if(!next.validate(0,0,SIZE,SIZE)) continue;
      walls.add(next);
      seeds.remove(h);
      seeds.add(next);
    }
    for(var w:walls) map[w.x][w.y].blocked=true;
    return walls;
  }

  void drawbush(){
    var bushes=new HashSet<>(seed(RPG.high(1,8)));
    while(bushes.size()<SIZE*SIZE/20){
      var b=RPG.pick(bushes);
      b=RPG.pick(b.getadjacent());
      if(b.validate(0,0,SIZE,SIZE)&&!walls.contains(b)) bushes.add(b);
    }
    for(var b:bushes) map[b.x][b.y].obstructed=true;
  }

  /** TODO pull up to Map */
  void mirror(boolean horizontally,boolean vertically){
    if(horizontally) Collections.reverse(Arrays.asList(map));
    if(vertically) for(var tiles:map) Collections.reverse(Arrays.asList(tiles));
  }

  /** TODO pull up to Map */
  void rotate(){
    mirror(RPG.chancein(2),RPG.chancein(2));
  }

  @Override
  public void generate(){
    walls=drawwalls();
    drawbush();
    drawsea();
    rotate();
  }
}
