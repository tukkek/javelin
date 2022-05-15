package javelin.controller.content.map.location.town;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.Debug;
import javelin.controller.Point;
import javelin.controller.challenge.Tier;
import javelin.controller.content.fight.Siege;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * Used during {@link Town} {@link Siege}s. Scales with Town {@link Rank}.
 *
 * @author alex
 */
public class TownMap extends Map{
  /** @see #wall */
  public static final Image WALL=Images.get(List.of("terrain","shipfloor"));

  class Building{
    void draw(int xp,int yp,int width,int height){
      for(var x=xp;x<=xp+width;x++)
        for(var y=yp;y<=yp+height;y++) map[x][y].blocked=true;
    }
  }

  class RuinedBuilding extends Building{
    @Override
    void draw(int xp,int yp,int width,int height){
      var maxx=xp+width;
      var maxy=yp+height;
      var walls=new ArrayList<Point>(width+height*2);
      for(var x=xp;x<=maxx;x++) for(var y=yp;y<=maxy;y++)
        if(x==xp||y==yp||x==maxx||y==maxy) walls.add(new Point(x,y));
      Collections.shuffle(walls);
      var openings=RPG.r(2,walls.size()/2);
      for(var i=0;i<openings;i++) walls.remove(0);
      for(Point p:walls) map[p.x][p.y].blocked=true;
    }
  }

  class Pond extends Building{
    @Override
    void draw(int xp,int yp,int width,int height){
      for(var x=xp;x<=xp+width;x++)
        for(var y=yp;y<=yp+height;y++) map[x][y].obstructed=true;
      for(var x=xp+1;x<=xp+width-1;x++) for(var y=yp+1;y<=yp+height-1;y++){
        map[x][y].obstructed=false;
        map[x][y].flooded=true;
      }
    }
  }

  class Park extends Building{
    @Override
    void draw(int xp,int yp,int width,int height){
      for(var x=xp+1;x<=xp+width-1;x++)
        for(var y=yp+1;y<=yp+height-1;y++) map[x][y].obstructed=true;
    }
  }

  class Empty extends Building{
    @Override
    void draw(int xp,int yp,int width,int height){
      //nope, vacant slot
    }
  }

  class Crates extends Building{
    @Override
    void draw(int xp,int yp,int width,int height){
      //TODO could turn this into Obstacles, when added
      var density=RPG.r(25,50)/100f;
      for(var x=xp;x<=xp+width;x++)
        for(var y=yp;y<=yp+height;y++) map[x][y].blocked=RPG.random()<density;
    }
  }

  static final int PLACEMENTS=10_000;

  final Building COMMON=new Building();
  final List<Building> UNCOMMON=List.of(new RuinedBuilding(),new Crates());
  final List<Building> RARE=List.of(new Pond(),new Park(),new Empty());

  HashSet<Point> used=new HashSet<>();
  int border;
  int minsize=3;
  int maxsize;
  int area;
  float density;
  float foliage;

  /**
   * @param t Generates a Town {@link Siege} map.
   */
  public TownMap(Tier t){
    this(t.getordinal()+1);
  }

  /**
   * Used for {@link Debug}ging purposes.
   *
   * @param rank See {@link Rank#rank}.
   *
   * @see Town#getrank()
   */
  public TownMap(int rank){
    super("Town map",DndMap.SIZE,DndMap.SIZE);
    floor=Images.get(List.of("terrain","towngrass"));
    obstacle=Images.get(List.of("terrain","bush"));
    wall=WALL;
    define(rank);
  }

  void define(int rank){
    if(rank==Rank.CITY.rank) border=0;
    else{
      border=map.length/(rank+1);
      border/=2; //half on each side
    }
    maxsize=minsize+rank-1;
    area=map.length*map[0].length-border*border;
    density=new float[]{.2f,.33f,.75f,1f}[rank-1];
    foliage=1/(rank+1f);
  }

  boolean iscenter(int x,int y){
    return border<x&&x<map.length-border&&border<y&&y<map.length-border;
  }

  boolean isborder(int x,int y){
    return !iscenter(x,y);
  }

  @Override
  public void generate(){
    var minx=border+1;
    var maxx=map.length-border-1;
    var miny=minx;
    var maxy=map[0].length-border-1;
    for(var i=0;i<PLACEMENTS;i++){
      if(used.size()>=area*density) break;
      var x=RPG.r(minx,maxx);
      var y=RPG.r(miny,maxy);
      var width=minsize;
      while(RPG.chancein(2)&&width<maxsize) width+=1;
      var height=minsize;
      while(RPG.chancein(2)&&height<maxsize) height+=1;
      placebuilding(x,y,width,height);
    }
    placefoliage();
  }

  void placefoliage(){
    var foliage=new HashSet<Point>(Math.round(border*border*this.foliage));
    for(var x=0;x<map.length;x++) for(var y=0;y<map[0].length;y++)
      if(isborder(x,y)&&RPG.random()<this.foliage) foliage.add(new Point(x,y));
    for(var x=border;x<=map.length-border;x++)
      for(var y=border;y<=map[0].length-border;y++)
        foliage.remove(new Point(x,y)); //clear 1 tile around border
    for(Point p:foliage) map[p.x][p.y].obstructed=true;
  }

  void placebuilding(int xp,int yp,int width,int height){
    var area=new HashSet<Point>(width*height);
    for(var x=xp;x<=xp+width;x++) for(var y=yp;y<=yp+height;y++){
      var p=new Point(x,y);
      if(!iscenter(p.x,p.y)||used.contains(p)) return;
      area.add(p);
    }
    for(var x=xp-1;x<=xp+width+1;x++)
      for(var y=yp-1;y<=yp+height+1;y++) used.add(new Point(x,y));//add spacing as well
    used.addAll(area);
    selectbuilding().draw(xp,yp,width,height);
  }

  Building selectbuilding(){
    var type=RPG.r(1,6);
    if(type==1) return RPG.pick(RARE);
    if(type==2||type==3) return RPG.pick(UNCOMMON);
    return COMMON;
  }
}
