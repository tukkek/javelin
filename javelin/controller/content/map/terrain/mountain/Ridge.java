package javelin.controller.content.map.terrain.mountain;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.controller.content.map.Section;
import javelin.controller.content.map.Section.Sections;
import javelin.controller.content.map.terrain.hill.HillShore;
import javelin.controller.exception.GaveUp;
import javelin.model.state.Square;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * TODO this could work as a "Volcano Bridge" if you add a entrance/exit on the
 * edges - and maybe change {@link Square#blocked} with damaging lava with
 * {@link Square#flooded}.
 *
 * @author alex
 */
public class Ridge extends Map{
  static final int SIZE=DndMap.SIZE;
  static final int MID=SIZE/2;
  static final HashSet<Point> AREA=Point.getrange(0,0,SIZE,SIZE);

  /** Constructor. */
  public Ridge(){
    super("Mountain ridge",SIZE,SIZE);
    floor=Images.get(List.of("terrain","ruggedwall"));
    wall=Images.get(List.of("terrain","orcwall"));
    obstacle=Images.get(List.of("terrain","rock2"));
  }

  void segment(int from,int to){
    var area=Point.getrange(from,0,to,SIZE);
    var segments=new HashSet<Point>((from+to)*SIZE);
    while(segments.isEmpty()) try{
      var sections=new Sections(Section.class,this);
      sections.segment(20,.4,area);
      for(var s:sections) if(s.area.size()>1) segments.addAll(s.area);
    }catch(GaveUp e){
      segments.clear();
    }
    for(var s:segments) map[s.x][s.y].blocked=false;
  }

  void obstruct(){
    var empty=new HashSet<>(
        AREA.stream().filter(a->!map[a.x][a.y].blocked).collect(toList()));
    Collection<Point> full=new HashSet<>(AREA);
    full.removeAll(empty);
    while(!full.isEmpty()){
      var obstruct=full.stream().flatMap(f->f.getadjacent().stream())
          .filter(f->empty.contains(f)).collect(toSet());
      empty.removeAll(obstruct);
      full=new HashSet<>();
      for(var o:obstruct) if(RPG.chancein(3)){
        map[o.x][o.y].obstructed=true;
        full.add(o);
      }
    }
  }

  @Override
  public void generate(){
    for(var p:AREA) map[p.x][p.y].blocked=true;
    var width=RPG.low(1,4)+2;
    var from=MID-width;
    var to=MID+width;
    var ridge=Point.getrange(from,0,to,SIZE);
    for(var r:ridge) map[r.x][r.y].blocked=false;
    segment(0,from);
    segment(to,SIZE);
    for(var p:AREA){
      var tile=map[p.x][p.y];
      if(tile.blocked&&RPG.chancein(3)) tile.blocked=false;
    }
    for(var r:RPG.shuffle(new ArrayList<>(ridge)))
      if(RPG.chancein(10)&&r.getorthogonallyadjacent().stream()
          .filter(a->a.validate(0,0,SIZE,SIZE)&&map[a.x][a.y].blocked).findAny()
          .isPresent())
        map[r.x][r.y].blocked=true;
    obstruct();
    HillShore.rotate(this);
  }
}
