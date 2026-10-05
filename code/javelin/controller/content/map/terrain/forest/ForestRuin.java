package javelin.controller.content.map.terrain.forest;

import java.util.List;

import javelin.controller.Point;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.controller.content.terrain.Terrain;
import javelin.old.RPG;
import javelin.view.Images;

/** Map hierarchy that can be alse used for other {@link Terrain}. */
public class ForestRuin extends Map{
  static final List<Double> PERIODS=List.of(3.0,5.0);

  /** Percont modifier for quantity of "plants". */
  public double plants=1.0;
  /** "Plant" growth cycles. */
  public int growths=1;

  /** Sub-class constructor. */
  public ForestRuin(String namep){
    super(namep,DndMap.SIZE,DndMap.SIZE);
    floor=Images.get(List.of("terrain","arena"));
    wall=Images.get(List.of("terrain","tree"));
    wallfloor=Images.get(List.of("terrain","dirt"));
    obstacle=Images.get(List.of("terrain","bush2"));
  }

  /** Constructor. */
  public ForestRuin(){
    this("Forest ruin");
  }

  /** @return "Plant" size. */
  public int size(){
    var s=2;
    for(var i=0;i<growths;i++) while(RPG.chancein(2)) s+=1;
    return s;
  }

  void plant(int fromx,int tox,int fromy,int toy){
    var p=new Point(RPG.r(fromx,tox-1),RPG.r(fromy,toy-1));
    var width=size();
    var height=size();
    for(var x=p.x;x<p.x+width;x++) for(var y=p.y;y<p.y+height;y++)
      if(new Point(x,y).validate(tox,toy)&&!RPG.chancein(6)) putwall(x,y);
  }

  @Override
  public void generate(){
    var all=Point.getrange(width,height);
    for(var p:all) putwall(p.x,p.y);
    var borderw=width*RPG.low(2,4)/10;
    var borderh=width*RPG.low(2,4)/10;
    var fromx=borderw/2;
    var tox=width-fromx;
    var fromy=borderh/2;
    var toy=height-fromy;
    for(var x=fromx;x<tox;x++) for(var y=fromy;y<toy;y++) map[x][y].clear();
    var nplants=all.size()*plants/100;
    for(var i=0;i<nplants;i++) plant(fromx,tox,fromy,toy);
    var period=Math.round(Math.ceil((width-borderw)/RPG.pick(PERIODS)));
    for(var x=fromx;x<tox-1;x++)
      if((x-fromx)/period%2!=0) for(var y=fromy;y>=0;y--) map[x][y].clear();
    for(var a:all){
      var x=a.x;
      var y=a.y;
      if(!map[x][y].blocked&&RPG.chancein(4)) putobstacle(x,y);
    }
    rotate();
  }
}
