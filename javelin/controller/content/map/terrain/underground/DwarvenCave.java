package javelin.controller.content.map.terrain.underground;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.controller.content.map.Section;
import javelin.controller.exception.GaveUp;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.model.state.Square;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * TODO similar patterns would make for a cool {@link FloorTile} as well.
 *
 * @author alex
 */
public class DwarvenCave extends Map{
  static final int SIZE=DndMap.SIZE;

  /** Constructor. */
  public DwarvenCave(){
    super("Dwarven cave",SIZE,SIZE);
    obstacle=rock;
    flooded=Images.get(List.of("terrain","aquatic"));
  }

  /** @return Segments of {@link Square}s along the map borders. */
  public static Set<Point> occupyborder(int seeds,int target){
    var outer=new HashSet<Point>(seeds);
    while(outer.size()<seeds){
      var p=new Point(RPG.r(SIZE),RPG.r(SIZE));
      if(RPG.chancein(2)) p.x=RPG.chancein(2)?0:SIZE-1;
      else p.y=RPG.chancein(2)?0:SIZE-1;
      outer.add(p);
    }
    while(outer.size()<target){
      var adjacent=RPG.pick(outer).getorthogonallyadjacent().stream()
          .filter(p->p.validate(0,0,SIZE,SIZE)&&!outer.contains(p)).toList();
      if(!adjacent.isEmpty()) outer.add(RPG.pick(adjacent));
    }
    return outer;
  }

  Set<Point> drawouterwalls(Set<Point> area){
    var outerarea=SIZE*4/10;
    var outer=occupyborder(outerarea,area.size()/3);
    for(var o:outer) map[o.x][o.y].blocked=true;
    return outer;
  }

  Set<Point> drawinnerwalls(Set<Point> outer,Set<Point> area) throws GaveUp{
    var inner=new HashSet<>(area);
    inner.removeAll(outer);
    var innerseeds=RPG.rolldice(2,4);
    var sections=Section.segment(innerseeds,.1,inner,this);
    inner.clear();
    for(var s:sections) inner.addAll(s.area);
    for(var i:inner) map[i.x][i.y].blocked=true;
    return inner;
  }

  List<Point> drawwater(Set<Point> empty) throws GaveUp{
    var patches=RPG.randomize(2,0,Integer.MAX_VALUE);
    var water=new ArrayList<Point>();
    for(var s:Section.segment(patches,.06,empty,this))
      for(var a:s.area) if(!map[a.x][a.y].blocked) water.add(a);
    for(var w:water) map[w.x][w.y].flooded=true;
    return water;
  }

  void drawrocks(Set<Point> empty) throws GaveUp{
    var patches=RPG.randomize(2,0,Integer.MAX_VALUE);
    for(var s:Section.segment(patches,.2,empty,this))
      for(var a:s.area) if(RPG.chancein(5)) map[a.x][a.y].obstructed=true;
  }

  @Override
  public void generate(){
    var area=Point.getrange(0,0,SIZE,SIZE);
    try{
      var outer=drawouterwalls(area);
      var inner=drawinnerwalls(outer,area);
      var empty=new HashSet<>(area);
      empty.removeAll(outer);
      empty.removeAll(inner);
      if(RPG.chancein(4)) empty.removeAll(drawwater(empty));
      drawrocks(empty);
    }catch(GaveUp e){
      for(var a:area){
        var m=map[a.x][a.y];
        m.blocked=false;
        m.flooded=false;
        m.obstructed=true;
      }
      generate();
    }
  }
}
