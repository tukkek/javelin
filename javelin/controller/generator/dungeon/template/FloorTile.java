package javelin.controller.generator.dungeon.template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.exception.GaveUp;
import javelin.controller.generator.dungeon.Direction;
import javelin.controller.generator.dungeon.DungeonArea;
import javelin.controller.generator.dungeon.DungeonGenerator;
import javelin.controller.generator.dungeon.template.Iterator.TemplateTile;
import javelin.controller.generator.dungeon.template.corridor.StraightCorridor;
import javelin.controller.generator.dungeon.template.corridor.WindingCorridor;
import javelin.controller.generator.dungeon.template.generated.Irregular;
import javelin.controller.generator.dungeon.template.generated.Linear;
import javelin.controller.generator.dungeon.template.generated.Rectangle;
import javelin.controller.generator.dungeon.template.mutator.Alcoves;
import javelin.controller.generator.dungeon.template.mutator.Grow;
import javelin.controller.generator.dungeon.template.mutator.Hallway;
import javelin.controller.generator.dungeon.template.mutator.HorizontalMirror;
import javelin.controller.generator.dungeon.template.mutator.Mutator;
import javelin.controller.generator.dungeon.template.mutator.Noise;
import javelin.controller.generator.dungeon.template.mutator.Rotate;
import javelin.controller.generator.dungeon.template.mutator.Symmetry;
import javelin.controller.generator.dungeon.template.mutator.VerticalMirror;
import javelin.controller.generator.dungeon.template.mutator.Wall;
import javelin.controller.table.dungeon.FloorTileTable;
import javelin.controller.table.dungeon.RoomSizeTable;
import javelin.old.RPG;

/**
 * TODO most templates should be read from file, not generated
 *
 * @author alex
 */
public abstract class FloorTile implements Cloneable,DungeonArea,Serializable{
  public static final Character FLOOR='.';
  public static final Character WALL='█';
  public static final Character DECORATION='!';
  public static final Character DOOR='□';

  /** Procedurally generated templates only. */
  public static final List<FloorTile> GENERATED=List.of(new Irregular(),
      new Rectangle(),new Linear());
  public static final List<FloorTile> CORRIDORS=List.of(new StraightCorridor(),
      new WindingCorridor());

  static final ArrayList<Mutator> MUTATORS=new ArrayList<>(
      Arrays.asList(Rotate.INSTANCE,HorizontalMirror.INSTANCE,
          VerticalMirror.INSTANCE,new Symmetry(),new Noise(),new Wall(),
          new Alcoves(),Grow.INSTANCE,new Hallway()));
  static final ArrayList<Mutator> ROTATORS=new ArrayList<>(Arrays.asList(
      Rotate.INSTANCE,HorizontalMirror.INSTANCE,VerticalMirror.INSTANCE));
  static final int FREEMUTATORS;

  static{
    var freemutators=0;
    for(Mutator m:MUTATORS) if(m.chance==null) freemutators+=1;
    FREEMUTATORS=freemutators;
  }

  public boolean corridor=false;
  public char[][] tiles=null;
  public double mutate=0.1;
  public int width=0;
  public int height=0;

  protected Character fill=FLOOR;
  public int doors=RPG.r(1,4);

  protected void init(int width,int height){
    this.width=width;
    this.height=height;
    tiles=new char[width][height];
    for(var x=0;x<width;x++) Arrays.fill(tiles[x],fill);
  }

  protected void initrandom(DungeonGenerator g){
    var table=g.floor.gettable(RoomSizeTable.class);
    init(table.rollnumber(),table.rollnumber());
  }

  public abstract void generate(DungeonGenerator g);

  public void modify(){
    if(FloorTileTable.DEBUGMUTATOR!=null){
      FloorTileTable.DEBUGMUTATOR.apply(this);
      return;
    }
    var chance=mutate/FREEMUTATORS;
    for(var m:RPG.shuffle(MUTATORS)){
      if(corridor&&!m.allowcorridor) continue;
      if(RPG.random()<(m.chance==null?chance:m.chance)) m.apply(this);
    }
  }

  @Override
  public String toString(){
    var s="";
    for(var y=height-1;y>=0;y--){
      for(var x=0;x<width;x++){
        Character c=tiles[x][y];
        s+=c==null?' ':c;
      }
      s+="\n";
    }
    return s;
  }

  public void iterate(Iterator i){
    for(var x=0;x<width;x++)
      for(var y=0;y<height;y++) i.iterate(new TemplateTile(x,y,tiles[x][y]));
  }

  protected double getarea(){
    return width*height;
  }

  public ArrayList<Point> find(char tile){
    var found=new ArrayList<Point>();
    for(var x=0;x<width;x++)
      for(var y=0;y<height;y++) if(tiles[x][y]==tile) found.add(new Point(x,y));
    return found;
  }

  public FloorTile create(DungeonGenerator g){
    try{
      FloorTile c=null;
      while(c==null||!c.validate()){
        c=clone();
        c.tiles=null;
        c.width=0;
        c.height=0;
        c.generate(g);
        c.modify();
        c.close();
        c.makedoors();
      }
      return c;
    }catch(GaveUp e){
      return null;
    }
  }

  /**
   * @return <code>true</code> if the generated template is good to use in an
   *   actual map.
   * @throws GaveUp Subclasses may throw, otherwise will continue calling
   *   {@link #create()} infinitely. Especially useful for sanitizing
   *   {@link StaticTemplate}s.
   */
  protected boolean validate() throws GaveUp{
    if(tiles==null) return false;
    var doors=getdoors();
    final var free=new HashSet<Point>(width*height);
    walk(doors.get(0),free);
    for(Point door:doors) if(!free.contains(door)) return false;
    iterate(t->{
      if(!free.contains(new Point(t.x,t.y))) tiles[t.x][t.y]=WALL;
    });
    return true;
  }

  void walk(Point tile,HashSet<Point> free){
    if(!free.add(tile)) return;
    for(Point step:Point.getadjacent2()){
      step.x+=tile.x;
      step.y+=tile.y;
      if(step.validate(0,0,width,height)&&tiles[step.x][step.y]!=WALL)
        walk(step,free);
    }
  }

  void makedoors() throws GaveUp{
    if(corridor&&doors==1) doors=2;
    var attempts=doors*4;
    while(attempts>0&&doors>0){
      var direction=Direction.getrandom();
      var door=findentry(direction);
      if(door==null) attempts-=1;
      else{
        tiles[door.x][door.y]=DOOR;
        doors-=1;
      }
    }
    if(attempts==0) tiles=null;
  }

  public int count(char c){
    var i=0;
    for(var x=0;x<width;x++) for(var y=0;y<height;y++) if(tiles[x][y]==c) i+=1;
    return i;
  }

  Point findentry(Direction d){
    var doors=d.getborder(this);
    Collections.shuffle(doors);
    for(Point door:doors){
      var p=new Point(door.x+d.reverse.x,door.y+d.reverse.y);
      if(tiles[p.x][p.y]==FLOOR&&!neardoor(p)) return door;
    }
    return null;
  }

  boolean neardoor(Point p){
    for(var x=p.x-1;x<=p.x+1;x++) for(var y=p.y-1;y<=p.y+1;y++){
      var neighbor=new Point(x,y);
      if(p.validate(0,0,width,height)&&tiles[neighbor.x][neighbor.y]==DOOR)
        return true;
    }
    return false;
  }

  @Override
  protected FloorTile clone(){
    try{
      return (FloorTile)super.clone();
    }catch(CloneNotSupportedException e){
      throw new RuntimeException(e);
    }
  }

  public void close(){
    if(isclosed()) return;
    width+=2;
    height+=2;
    var closed=new char[width][height];
    Arrays.fill(closed[0],WALL);
    Arrays.fill(closed[width-1],WALL);
    for(var x=1;x<width-1;x++){
      closed[x][0]=WALL;
      closed[x][height-1]=WALL;
    }
    for(var x=0;x<tiles.length;x++)
      for(var y=0;y<tiles[x].length;y++) closed[x+1][y+1]=tiles[x][y];
    tiles=closed;
  }

  boolean isclosed(){
    for(var x=0;x<width;x++)
      for(var y=0;y<height;y++) if(x==0||y==0||x==width-1||y==height-1)
        if(tiles[x][y]!=WALL) return false;
    return true;
  }

  protected boolean isborder(int x,int y){
    return x==0||y==0||x==width-1||y==height-1;
  }

  public List<Point> getdoors(){
    return find(FloorTile.DOOR);
  }

  public Point getdoor(Direction d){
    for(Point door:getdoors()) if(inborder(door.x,door.y)==d) return door;
    return null;
  }

  public Direction inborder(int x,int y){
    if(x==0) return Direction.WEST;
    if(x==width-1) return Direction.EAST;
    if(y==0) return Direction.SOUTH;
    if(y==height-1) return Direction.NORTH;
    return null;
  }

  public Point rotate(Direction to){
    Point todoor=null;
    while(todoor==null){
      todoor=getdoor(to);
      if(todoor==null) RPG.pick(ROTATORS).apply(this);
    }
    return todoor;
  }

  @Override
  public int getwidth(){
    return width;
  }

  @Override
  public int getheight(){
    return height;
  }

  @Override
  public int getx(){
    return 0;
  }

  @Override
  public int gety(){
    return 0;
  }

  public int countadjacent(Character tile,Point p){
    var found=0;
    for(var x=p.x-1;x<=p.x+1;x++) for(var y=p.y-1;y<=p.y+1;y++)
      if(new Point(x,y).validate(0,0,width,height)&&tile.equals(tiles[x][y]))
        found+=1;
    return found;
  }

  public void settiles(char[][] t){
    tiles=t;
    width=t.length;
    height=t[0].length;
  }
}
