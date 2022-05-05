package javelin.controller.content.map.terrain.mountain;

import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.controller.content.map.Section.Sections;
import javelin.controller.content.map.Section.ThinSection;
import javelin.controller.content.terrain.Mountains;
import javelin.controller.content.terrain.Water;
import javelin.controller.exception.GaveUp;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.mappanel.world.WorldTile;

/**
 * A {@link Mountains} map next to a {@link Water} {@link WorldTile}.
 *
 * @author alex
 */
public class MountainShore extends Map{
  static final int SIZE=DndMap.SIZE;
  static final int[] FROM={SIZE/10,SIZE*1/3};
  static final int[] TO={SIZE*2/3,SIZE*9/10};

  /** Constructor. */
  public MountainShore(){
    super("Mountain shore",SIZE,SIZE);
    flooded=Images.get(List.of("terrain","aquatic"));
    obstacle=Images.get(List.of("terrain","rock2"));
    floor=Images.get(List.of("terrain","desert"));
  }

  int bind(int value,int[] limit){
    if(value<limit[0]) return limit[0];
    if(value>=limit[1]) return limit[1]-1;
    return value;
  }

  void drawcliff(int shallowy,int mountainy){
    var fromx=RPG.low(FROM[0],FROM[1]);
    var tox=RPG.high(TO[0],TO[1]-1);
    for(var y=shallowy;y<SIZE;y++){
      for(var x=0;x<SIZE;x++){
        if(fromx<=x&&x<=tox) continue;
        map[x][y].flooded=true;
      }
      var wall=new HashSet<Point>(4);
      var from=new Point(fromx,y);
      var to=new Point(tox,y);
      wall.add(from);
      wall.add(to);
      var range=y<mountainy?2:1;
      wall.addAll(from.getadjacent(range));
      wall.addAll(to.getadjacent(range));
      for(var w:wall) if(w.validate(0,shallowy,SIZE,SIZE)){
        var m=map[w.x][w.y];
        m.flooded=false;
        if(y>=mountainy) m.blocked=true;
        else if(RPG.chancein(4)) m.obstructed=true;
      }
      fromx=bind(fromx+RPG.r(-2,+1),FROM);
      tox=bind(tox+RPG.r(-1,+2),TO);
    }
  }

  void drawrocks(int mountainy){
    var area=Point.getrange(SIZE,SIZE).stream()
        .filter(p->!map[p.x][p.y].flooded&&!map[p.x][p.y].blocked)
        .collect(toSet());
    var seeds=area.size()/10;
    if(seeds==0) return;
    Set<Point> rocks=null;
    while(rocks==null) try{
      var s=new Sections(ThinSection.class,this);
      s.segment(bind(seeds,new int[]{1,area.size()}),.2,area);
      rocks=s.getarea();
      for(var r:rocks){
        var m=map[r.x][r.y];
        if(r.y<mountainy||RPG.chancein(2)) m.obstructed=true;
        else m.blocked=true;
      }
    }catch(GaveUp e){
      continue;
    }
  }

  @Override
  public void generate(){
    var shallowy=RPG.r(0,SIZE/2);
    for(var p:Point.getrange(SIZE,shallowy)) map[p.x][p.y].flooded=true;
    var mountainy=RPG.high(shallowy,SIZE);
    drawcliff(shallowy,mountainy);
    drawrocks(mountainy);
  }
}
