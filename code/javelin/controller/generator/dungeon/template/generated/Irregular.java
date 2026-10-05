package javelin.controller.generator.dungeon.template.generated;

import java.util.Collections;
import java.util.LinkedList;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.DungeonGenerator;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.old.RPG;

public class Irregular extends FloorTile{
  private static final Point[] ADJACENT={new Point(-1,0),new Point(+1,0),
      new Point(0,-1),new Point(0,+1)};
  private static final int PERCENTMIN=15;
  private static final int PERCENTMAX=55;

  public Irregular(){
    mutate=.5;
  }

  @Override
  public void generate(DungeonGenerator g){
    width=0;
    while(width<3||height<3) initrandom(g);
    var ratio=RPG.r(PERCENTMIN,PERCENTMAX)/100.0;
    var expand=getborders();
    Collections.shuffle(expand);
    for(var i=0;i<expand.size()*ratio;i++){
      var border=expand.get(i);
      tiles[border.x][border.y]=WALL;
    }
    for(var count=count(WALL);!expand.isEmpty()
        &&count<getarea()*ratio;count=count(WALL)){
      var p=RPG.pick(expand);
      expand.remove(p);
      if(checkblock(p)) continue;
      tiles[p.x][p.y]=WALL;
      for(Point adjacent:new Point[]{new Point(p.x-1,p.y),new Point(p.x+1,p.y),
          new Point(p.x,p.y-1),new Point(p.x,p.y+1)})
        if(adjacent.validate(0,0,width,height)
            &&tiles[adjacent.x][adjacent.y]!=WALL)
          expand.add(adjacent);
    }
  }

  boolean checkblock(Point p){
    var walls=0;
    for(Point wall:ADJACENT){
      wall=new Point(p.x+wall.x,p.y+wall.y);
      if(!wall.validate(0,0,width,height)||tiles[wall.x][wall.y]==WALL)
        walls+=1;
    }
    return walls>=2;
  }

  LinkedList<Point> getborders(){
    final var borders=new LinkedList<Point>();
    iterate(t->{
      if(isborder(t.x,t.y)) borders.add(new Point(t.x,t.y));
    });
    return borders;
  }
}
