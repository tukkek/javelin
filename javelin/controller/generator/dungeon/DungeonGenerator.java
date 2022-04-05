package javelin.controller.generator.dungeon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.exception.GaveUp;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.controller.generator.dungeon.template.StaticTemplate;
import javelin.controller.generator.dungeon.template.corridor.StraightCorridor;
import javelin.controller.table.dungeon.FloorTileTable;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.RPG;
import javelin.view.screen.town.SelectScreen;

public class DungeonGenerator{
  static final boolean DEBUGROOMS=true;
  static final int DEBUGSIZE=1;
  static final int MAXATTEMPTS=9*1000;

  /**
   * TODO temporary: will need to be refactored when more than one level can be
   * generated (with one set of tables/parameters per level) and/or for
   * multithreading. Should be as simple as passing an instance of this or of a
   * new class GeneratorLevel to Templates.
   */
  public static DungeonGenerator instance;

  public VirtualMap map=new VirtualMap();
  public char[][] grid;
  public String ascii;
  /** Floor being mapped. */
  public DungeonFloor floor;

  LinkedList<Segment> segments=new LinkedList<>();
  ArrayList<FloorTile> pool=new ArrayList<>();
  String templatesused="";
  private int minrooms;
  private int maxrooms;
  private int nrooms;

  /**
   * @param maxrooms
   * @param minrooms
   * @param tables2
   * @param sizehint TOOD would be cool to have this handled built-in, not on
   *   {@link #generate(int, int)}.
   */
  private DungeonGenerator(int minrooms,int maxrooms,DungeonFloor f){
    this.minrooms=minrooms;
    this.maxrooms=maxrooms;
    floor=f;
    instance=this;
  }

  public void start() throws GaveUp{
    pool.addAll(floor.gettable(FloorTileTable.class).gettemplates());
    for(var t:pool) templatesused+=t.getClass().getSimpleName()+" ";
    draw();
    /* TODO make this a Table 5±10 */
    var connectionattempts=map.rooms.size()*RPG.r(0,10);
    for(var i=0;i<connectionattempts;i++) createconnection();
    finish();
  }

  FloorTile generateroom(){
    FloorTile t=null;
    while(t==null) t=RPG.pick(pool).create();
    return t;
  }

  /**
   * TODO doesn't need necesarily to create only based on rooms
   */
  void createconnection(){
    var r=RPG.pick(map.rooms);
    var d=Direction.getrandom();
    var exit=RPG.pick(d.getborder(r));
    if(map.countadjacent(FloorTile.FLOOR,exit)==0) return;
    var connection=new ArrayList<Point>();
    var length=RPG.r(1,4)+RPG.r(1,4)+1;
    var connected=false;
    for(var i=0;i<length;i++){
      var step=new Point(exit);
      step.x-=d.reverse.x*i;
      step.y-=d.reverse.y*i;
      if(map.countadjacent(FloorTile.DOOR,step)>0) return;
      connection.add(step);
      var tile=map.get(step);
      if(connection.size()>1&&map.countadjacent(FloorTile.FLOOR,step)==1){
        connected=true;
        break;
      }
      if(FloorTile.WALL.equals(tile)||tile==null) continue;
      return;
    }
    drawconnection(connection,connected);
  }

  void drawconnection(ArrayList<Point> connection,boolean connected){
    if(connected&&connection.size()>2){
      for(Point step:connection) map.set(FloorTile.FLOOR,step);
      var door=connection.get(connection.size()-1);
      map.set(FloorTile.DOOR,door);
    }
  }

  public void finish(){
    ascii=map.rasterize(true).replaceAll(" ",
        Character.toString(FloorTile.WALL));
    var grid=ascii.split("\n");
    this.grid=new char[grid.length][];
    for(var i=0;i<grid.length;i++) this.grid[i]=grid[i].toCharArray();
  }

  void draw() throws GaveUp{
    var start=generateroom();
    segments.add(new Segment(start,new Point(0,0)));
    map.draw(start,0,0);
    nrooms=RPG.r(minrooms,maxrooms);
    var attempts=0;
    while(nrooms>0&&!segments.isEmpty()){
      attempts+=1;
      if(attempts>=MAXATTEMPTS){
        if(Javelin.DEBUG)
          System.out.println("Max dungeon generation attempts reached");
        throw new GaveUp();
      }
      var s=RPG.pick(segments);
      segments.remove(s);
      var doors=new LinkedList<>(s.room.getdoors());
      Collections.shuffle(doors);
      placingdoors:while(!doors.isEmpty()){
        var door=doors.pop();
        for(var i=0;i<10;i++) if(expandroom(door,s)) continue placingdoors;
        if(map.get(s.cursor,door).equals(FloorTile.DOOR))
          map.set(FloorTile.WALL,s.cursor,door);
      }
    }
    for(Segment s:segments) for(Point door:s.room.getdoors())
      if(map.get(s.cursor,door).equals(FloorTile.DOOR))
        map.set(FloorTile.WALL,s.cursor,door);
  }

  boolean expandroom(Point door,Segment s){
    var next=generateroom();
    var going=s.room.inborder(door.x,door.y);
    if(going==null){
      /* static template with internal door */
      map.set(FloorTile.FLOOR,door.x,door.y);
      return true;
    }
    var coming=Direction.opposite(going);
    var doorb=next.rotate(coming);
    var cursorb=new Point(s.cursor);
    cursorb=going.connect(cursorb,s.room,next,door,doorb);
    StraightCorridor.clear(s.room,s.cursor,door,next,doorb,map);
    if(!map.draw(next,cursorb.x,cursorb.y)) return false;
    map.set(FloorTile.FLOOR,cursorb,doorb);
    segments.add(new Segment(next,cursorb));
    nrooms-=1;
    return true;
  }

  void print(){
    var lines=ascii.split("\n");
    var map=new char[lines.length][];
    for(var i=0;i<lines.length;i++) map[i]=lines[i].toCharArray();
    if(DEBUGROOMS){
      var rooms=this.map.rooms;
      for(var i=0;i<rooms.size();i++){
        var r=rooms.get(i);
        for(var x=r.x;x<r.x+r.width;x++) for(var y=r.y;y<r.y+r.height;y++)
          if(map[x][y]==FloorTile.FLOOR) map[x][y]=SelectScreen.getkey(i);
      }
    }
    var builder=new StringBuilder();
    for(char[] line:map){
      builder.append(line);
      builder.append('\n');
    }
    System.out.println(ascii);
  }

  /**
   * @return A dungeon map, ready for drawing.
   * @see VirtualMap#rooms
   */
  public static DungeonGenerator generate(int minrooms,int maxrooms,
      DungeonFloor f){
    StaticTemplate.load();
    DungeonGenerator dungeon=null;
    while(dungeon==null) try{
      dungeon=new DungeonGenerator(minrooms,maxrooms,f);
      dungeon.start();
      var size=dungeon.map.rooms.size();
      if(!(minrooms<=size&&size<=maxrooms)){
        dungeon=null;
        if(Javelin.DEBUG) System.out.println("Wrong size: "+size);
      }
    }catch(GaveUp e){
      continue;
    }
    return dungeon;
  }

  public static void main(String[] args) throws IOException{
    var minrooms=3;
    var maxrooms=7;
    minrooms=13;
    maxrooms=13*2;
    var dungeon=generate(minrooms,maxrooms,null);
    dungeon.print();
    System.out.println(dungeon.templatesused);
  }

  @Override
  public String toString(){
    return map.toString();
  }
}
