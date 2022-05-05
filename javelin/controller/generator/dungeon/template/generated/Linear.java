package javelin.controller.generator.dungeon.template.generated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.Direction;
import javelin.controller.generator.dungeon.DungeonGenerator;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.old.RPG;

public class Linear extends FloorTile{
  static final float MAXDISTANCE=1/3f;
  protected int minsize=7;

  public Linear(){
    mutate=0.5;
  }

  @Override
  public void generate(DungeonGenerator g){
    setupsize(g);
    var borders=getdots();
    draw(new LinkedList<>(borders));
    fill();
  }

  void setupsize(DungeonGenerator g){
    initrandom(g);
    while(width*height<minsize*minsize)
      init(width+RPG.r(1,4),height+RPG.r(1,4));
  }

  List<Point> getdots(){
    List<Point> borders=new ArrayList<>();
    for(Direction d:Direction.DIRECTIONS) borders.addAll(d.getborder(this));
    var sides=Integer.MAX_VALUE;
    sides=RPG.r(Math.min(width,height),width+height);
    Collections.shuffle(borders);
    borders=borders.subList(0,sides);
    for(Point p:borders) bump(p);
    return borders;
  }

  void draw(LinkedList<Point> dots){
    var first=dots.pop();
    var from=first;
    while(!dots.isEmpty()){
      var to=findclosest(dots,from);
      connect(from,to);
      dots.remove(to);
      from=to;
    }
    connect(from,first);
  }

  void fill(){
    for(Direction d:Direction.DIRECTIONS) for(Point outer:d.getborder(this))
      while(outer.validate(0,0,width,height)&&tiles[outer.x][outer.y]==FLOOR){
        tiles[outer.x][outer.y]=WALL;
        outer.x+=d.reverse.x;
        outer.y+=d.reverse.y;
      }
  }

  void connect(Point step,Point to){
    step=step.clone();
    while(!step.equals(to)){
      tiles[step.x][step.y]=WALL;
      if(step.x!=to.x) step.x+=to.x>step.x?+1:-1;
      if(step.y!=to.y) step.y+=to.y>step.y?+1:-1;
    }
  }

  Point findclosest(LinkedList<Point> dots,Point first){
    var closest=dots.get(0);
    var distance=closest.distance(first);
    for(var i=1;i<dots.size();i++){
      var p=dots.get(i);
      var distancep=p.distance(first);
      if(distancep<distance){
        distance=distancep;
        closest=p;
      }
    }
    return closest;
  }

  protected void bump(Point p){
    var maxdeltax=Math.round(width*MAXDISTANCE);
    var maxdeltay=Math.round(height*MAXDISTANCE);
    var set=new HashSet<Point>();
    Point bump=null;
    while(bump==null||set.contains(bump)){
      bump=p.clone();
      if(p.x==0) bump.x+=RPG.r(0,maxdeltax);
      else if(p.x==width-1) bump.x-=RPG.r(0,maxdeltax);
      else if(p.y==0) bump.y+=RPG.r(0,maxdeltay);
      else bump.x-=RPG.r(0,maxdeltay);
    }
    if(bump.validate(0,0,width,height)){
      p.x=bump.x;
      p.y=bump.y;
    }
  }
}
