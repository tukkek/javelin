package javelin.controller.content.map;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.ai.BattleAi;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.map.terrain.water.DeepWaters;
import javelin.controller.content.terrain.Terrain;
import javelin.model.state.BattleState;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * Representation of a battle area.
 *
 * @author alex
 */
public abstract class Map{
  /** Background image. */
  public Image floor=Images.get(List.of("terrain","dirt"));
  /** Represents {@link Square#blocked}. */
  public Image wall=Images.get(List.of("terrain","wall"));
  /**
   * If given, instead of using {@link #wall} on top of a {@link #floor}
   * bakcground will use this instead.
   */
  public Image wallfloor=null;
  /** Represents an {@link Square#obstructed} item. */
  public Image obstacle=Images.get(List.of("terrain","bush"));
  /** Used often as an {@link #obstacle}. */
  public Image rock=Images.get(List.of("terrain","rock"));
  /** Represents {@link Square#flooded}. */
  public Image flooded=Images.get(List.of("terrain","flooded"));

  /**
   * Two-dimensional array of squares. map[point.x][point.y]
   */
  public Square[][] map;
  /** See {@link Weather}. By default allows any extent of flooding. */
  public int maxflooding=Weather.STORM;

  /** Map title. */
  public String name;

  /**
   * Maps that are supposed to be good for any minigame or situation.
   *
   * @see #random()
   */
  public boolean standard=true;

  /**
   * Usually <code>true</code> but confined spaces where flyers cannot fly over
   * walls will be <code>false</code>.
   *
   * This is done for consistency but mostly because it allows for the
   * {@link BattleAi} to just stay out of reach with flying creatures instead of
   * losing a fight. {@link DungeonFloor} maps for example can have wall
   * placement that makes it very hard to kill a flying unit unless you have one
   * yourself.
   *
   * @see Monster#fly
   */
  public boolean flying=true;

  /**
   * Construcor based on map size. By default all {@link Square}s are completely
   * free.
   */
  public Map(String namep,int width,int height){
    name=namep;
    map=new Square[width][height];
    for(var x=0;x<width;x++) for(var y=0;y<height;y++) map[x][y]=new Square();
  }

  /**
   * Creates the {@link #map}. Called after construction to allow for fast
   * instantiation of a {@link Maps} list and lazy generation operation.
   */
  abstract public void generate();

  /** Marks {@link Square#flooded}. */
  public void putwater(int x,int y){
    map[x][y].flooded=true;
  }

  /** Marks {@link Square#obstructed}. */
  final public void putobstacle(int x,int y){
    map[x][y].obstructed=true;
  }

  /**
   * @param x TODO
   * @param y TODO
   * @return {@link #obstacle} representation for this map. May be overridden
   *   for dynamic purposes.
   */
  public Image getobstacle(int x,int y){
    return obstacle;
  }

  /** Marks {@link Square#blocked}. */
  final public void putwall(int x,int y){
    map[x][y].blocked=true;
  }

  /**
   * @return Image that represents {@link Square#blocked}.
   * @see #wall
   * @see #wallfloor
   */
  public Image getblockedtile(int x,int y){
    return wallfloor==null?wall:wallfloor;
  }

  /**
   * @return <code>false</code> if this map can't be used now due to any
   *   circumstances.
   */
  public boolean validate(){
    return true;
  }

  @Override
  public boolean equals(Object obj){
    return name.equals(((Map)obj).name);
  }

  @Override
  public int hashCode(){
    return name.hashCode();
  }

  /** @return <code>true</code> if a valid coordinate. */
  public boolean validate(int x,int y){
    return 0<=x&&x<map.length&&0<=y&&y<map[0].length;
  }

  /**
   * @return A random map, excluding ones that are usually not recommended for
   *   typical fights like {@link DeepWaters}.
   */
  public static Map random(){
    var terrains=RPG.shuffle(new ArrayList<>(Terrain.NONWATER));
    for(var t:terrains){
      var m=t.getmap();
      if(m!=null) return m;
    }
    return null;
  }

  @Override
  public String toString(){
    var map="";
    for(Square[] element:this.map){
      for(Square s:element) if(s.blocked) map+='#';
      else if(s.obstructed) map+='.';
      else if(s.flooded) map+='~';
      else map+=' ';
      map+="\n";
    }
    return map;
  }

  /**
   * Seals off the outer border of the map.
   *
   * @return Squares borded off.
   */
  protected HashSet<Point> close(){
    var width=map.length;
    var height=map[0].length;
    var border=new HashSet<Point>((width+height)*2-4);
    for(var x=0;x<width;x++){
      map[x][0].blocked=true;
      border.add(new Point(x,0));
      map[x][width-1].blocked=true;
      border.add(new Point(x,width-1));
    }
    for(var y=0;y<height;y++){
      map[0][y].blocked=true;
      border.add(new Point(0,y));
      map[height-1][y].blocked=true;
      border.add(new Point(height-1,y));
    }
    return border;
  }

  /** @return Any random point in this map. */
  protected Point getrandompoint(){
    return new Point(RPG.r(0,map.length-1),RPG.r(0,map[0].length-1));
  }

  /** @return A non {@link Square#blocked} {@link #getrandompoint()}. */
  protected Point getempty(){
    Point p=null;
    while(p==null||map[p.x][p.y].blocked) p=getrandompoint();
    return p;
  }

  /** @return Total map area in tiles (width times height). */
  protected int getarea(){
    return map.length*map[0].length;
  }

  /** @return Floor image for the given coordinate (usually {@link #floor}). */
  public Image getfloor(int x,int y){
    return floor;
  }

  /**
   * @param team {@link BattleState#blueteam} or {@link BattleState#redteam}.
   * @return Spawn points for the given team.
   */
  public List<Point> getspawn(List<Combatant> team){
    var s=Fight.state;
    if(!team.isEmpty()){
      var byproximity=team.stream()
          .flatMap(c->c.getlocation().getadjacent().stream()).distinct()
          .filter(p->s.isempty(p.x,p.y)).collect(Collectors.toSet());
      if(!byproximity.isEmpty()) return new ArrayList<>(byproximity);
    }
    var byvision=s.getcombatants().stream()
        .flatMap(c->c.calculatevision(s).stream()).distinct()
        .filter(p->s.isempty(p.x,p.y)&&s.getsurroundings(p).isEmpty())
        .collect(Collectors.toList());
    if(!byvision.isEmpty()) return byvision;
    var random=Point.getrange(0,0,map.length,map[0].length);
    return RPG.shuffle(new ArrayList<>(random)).stream()
        .filter(p->s.isempty(p.x,p.y)&&s.getsurroundings(p).isEmpty()).limit(3)
        .collect(Collectors.toList());
  }

  /** @return All non-{@link Square#blocked} points. */
  protected List<Point> getallempty(){
    return Point.getrange(0,0,map.length,map[0].length).stream()
        .filter(p->!map[p.x][p.y].blocked).collect(Collectors.toList());
  }

  /**
   * 1/2 chance of mirroring the map vertically, 1/2 chance of mirroring it
   * horizontally and 1/4 chance each of rotating it 0°, 90°. 180°. 270°.
   *
   * Note that this only rotates the {@link Square} arrays, any other
   * coordinates will need to be adjusted manually or calculated or updated
   * after rotating.
   *
   * From https://stackoverflow.com/a/39212120
   */
  protected void rotate(){
    if(RPG.chancein(2)) Collections.reverse(Arrays.asList(map));
    if(RPG.chancein(2))
      for(var tiles:map) Collections.reverse(Arrays.asList(tiles));
    for(var rotations=RPG.r(1,4);rotations>1;rotations--)
      for(var i=0;i<map.length/2;i++) for(var j=0;j<map.length-1-2*i;j++){
        var tmp=map[j+i][map.length-1-i];
        map[j+i][map.length-1-i]=map[i][j+i];
        map[i][j+i]=map[map.length-1-j-i][i];
        map[map.length-1-j-i][i]=map[map.length-1-i][map.length-1-j-i];
        map[map.length-1-i][map.length-1-j-i]=tmp;
      }
  }
}
