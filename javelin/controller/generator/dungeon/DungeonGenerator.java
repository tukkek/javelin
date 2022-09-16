package javelin.controller.generator.dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.exception.GaveUp;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.controller.generator.dungeon.template.StaticTemplate;
import javelin.controller.generator.dungeon.template.corridor.StraightCorridor;
import javelin.controller.table.dungeon.FloorTileTable;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.RPG;
import javelin.view.screen.town.SelectScreen;

/** Generates a {@link Dungeon} map. */
public class DungeonGenerator{
  static final boolean DEBUGROOMS=true;
  static final int MAXATTEMPTS=10000;
  static final int DEBUGSIZE=1;

  /** Relative map. */
  public VirtualMap map=new VirtualMap();
  /** Floor being mapped. */
  public DungeonFloor floor;
  /** Fixed map. */
  public char[][] grid;
  /** Visual representation. */
  public String ascii;

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
  }

  /** Generates {@link Dungeon} map. */
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
    while(t==null) t=RPG.pick(pool).create(this);
    return t;
  }

  /** TODO doesn't need necesarily to create only based on rooms */
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

  /** Generates {@link #ascii} and {@link #grid}. */
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
        if(Javelin.DEBUG){
          var x="Max dungeon generation attempts reached";
          System.out.println(x);
        }
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
   * @param min Minimum number of rooms.
   * @param max Maximum number of rooms.
   * @return A dungeon map, ready for drawing.
   * @see VirtualMap#rooms
   */
  public static DungeonGenerator generate(int min,int max,DungeonFloor f){
    StaticTemplate.load();
    DungeonGenerator dungeon=null;
    var attempts=0;
    while(dungeon==null) try{
      dungeon=new DungeonGenerator(min,max,f);
      dungeon.start();
      var size=dungeon.map.rooms.size();
      if(!(min<=size&&size<=max)){
        attempts+=1;
        if(attempts>MAXATTEMPTS) throw new GaveUp();
        dungeon=null;
      }
    }catch(GaveUp e){
      throw new RestartWorldGeneration();
    }
    return dungeon;
  }

  /** Unit-test helper. */
  public static void main(String[] args){
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
