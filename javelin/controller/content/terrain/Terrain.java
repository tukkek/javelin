package javelin.controller.content.terrain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.challenge.Difficulty;
import javelin.controller.content.action.world.WorldMove;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.RandomEncounter;
import javelin.controller.content.map.Map;
import javelin.controller.content.map.Maps;
import javelin.controller.content.terrain.hazard.Hail;
import javelin.controller.content.terrain.hazard.Hazard;
import javelin.controller.db.EncounterIndex;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.generator.encounter.Encounter;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Survival;
import javelin.model.world.Actor;
import javelin.model.world.Season;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.productive.Mine;
import javelin.old.RPG;

/**
 * Represent different types of {@link World} terrain.
 *
 * One of the responsibilities of subclasses is to generate {@link World} areas.
 * The world generation method is pretty arbitrary, though it's based on
 * real-life geological considerations (like which area is usually close or
 * distant to each area) but not too worried with realist. Read the
 * documentation for methods starting with generate- for more information.
 *
 * Each terrain also offer hazards and events, which occur about 1% of the step,
 * but are only really triggered if conditions are right.
 *
 * @author alex
 */
public abstract class Terrain implements Serializable{
  /**
   * Description return by {@link #describeweather()} in case of
   * {@link Season#WINTER} snow.
   */
  public static final String SNOWING="snowing";

  /**
   * 1 in chance in X of a special {@link #gethazards(int, boolean)} ocurring.
   */
  public static final int HAZARDCHANCE=100;

  /** 2/16 Easy (el-5 to el-8) - plains */
  public static final Terrain PLAIN=new Plains();
  /** Similar to plains. */
  public static final Terrain HILL=new Hill();
  /** * 10/16 Moderate (el-4) - forest */
  public static final Terrain FOREST=new Forest();
  /** Similar to {@link #FOREST}. */
  public static final Terrain WATER=new Water();

  /** 3/16 Difficult (el-3 to el) - mountains */
  public static final Terrain MOUNTAINS=new Mountains();
  /** Similar to mountain. Doubles as tundra in the winter. */
  public static final Terrain DESERT=new Desert();
  /** 1/16 Very difficult (el+1) - swamp */
  public static final Terrain MARSH=new Marsh();
  /** Represent {@link DungeonFloor}s and {@link Mine}s. */
  public static final Terrain UNDERGROUND=new Underground();

  /** All terrain types. */
  public static final Terrain[] ALL={PLAIN,HILL,FOREST,MOUNTAINS,DESERT,MARSH,
      UNDERGROUND,WATER};
  /** All terrain types except {@link #WATER} and {@link #UNDERGROUND}. */
  public static final Terrain[] STANDARD={PLAIN,HILL,FOREST,MOUNTAINS,DESERT,
      MARSH};
  /** All terrain types except {@link #UNDERGROUND}. */
  public static final Terrain[] NONUNDERGROUND={PLAIN,HILL,FOREST,MOUNTAINS,
      DESERT,MARSH,WATER};
  /** All terrain types except {@link #water}. */
  public static final List<Terrain> NONWATER=List.of(PLAIN,HILL,FOREST,
      MOUNTAINS,DESERT,MARSH,UNDERGROUND);

  static final int[] STEPS={-1,0,+1};

  /** No road. */
  public Float movement=null;
  /** SRD name. Used to determine tile. */
  public String name=null;
  /**
   * Maximum encounter level delta allowed, in order to make some terrains more
   * noob-friendly.
   *
   * TODO do we really need this? isn't the random distribution enough? also:
   * isn't this making the game easier than it needs to be - and if it isn't,
   * should difficulty be addressed through some other means?
   */
  public Integer difficultycap=Integer.MAX_VALUE;
  /** Used to see distant {@link World} terrain. */
  public Integer visionbonus=null;
  /** ASCII representation of terrain type for debugging purposes. */
  public Character representation=null;
  /** Terrains that "overflow". They receive a "shore" visual. */
  public boolean liquid=false;
  /** A bonus to be added manually to {@link Survival} rolls. */
  public int survivalbonus=0;
  /** @see RandomEncounter */
  public Boolean safe=false;
  /** Human-friendly name, as in "Location is in the %s". */
  public String description;
  /** {@link Fight} {@link Map}s. */
  public Maps maps;
  /** As {@link #maps} but next to {@link Water}. */
  public Maps shoremaps;

  /** Constructor. */
  protected Terrain(String name,Maps maps,Maps shoremaps){
    this.name=name;
    description=name;
    this.maps=maps;
    this.shoremaps=shoremaps;
  }

  /**
   * Uses current terrain as base.
   *
   * @param mph Applies terrain penalty to base Squad speed.
   * @param x {@link World} coordinate.
   * @param y {@link World} coordinate.
   * @return Speed in miles per hour to traverse this terrain.
   *
   * @see Squad#move(boolean, Terrain, int, int)
   * @see Terrain#current()
   */
  public int speed(int mph,int x,int y){
    return Math.round(mph*movement);
  }

  /**
   * TODO this probably should return {@link Underground} as well.
   *
   * @return Current terrain difficulty. For example: {@link PLAIN}.
   */
  static public Terrain current(){
    if(JavelinApp.context==null) return null;
    var h=JavelinApp.context.getsquadlocation();
    return h==null?null:Terrain.get(h.x,h.y);
  }

  /**
   * @param x {@link World} coordinate.
   * @param y {@link World} coordinate.
   * @return {@link Underground} if there is a {@link Dungeon#active} or the
   *   {@link World} terrain.
   */
  public static Terrain get(int x,int y){
    return Dungeon.active==null?World.getseed().map[x][y]:Terrain.UNDERGROUND;
  }

  @Override
  public String toString(){
    return name;
  }

  @Override
  public int hashCode(){
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj){
    return obj!=null&&name.equals(((Terrain)obj).name);
  }

  HashSet<Point> generatearea(World world){
    var source=generatesource(world);
    var current=source;
    var area=generatestartingarea(world);
    var size=getareasize();
    while(area.size()<size){
      area.add(current);
      current=expand(area,world);
    }
    return area;
  }

  /**
   * @return Number of tiles the generated area for this terrain should have.
   */
  protected int getareasize(){
    return World.SIZE*World.SIZE/WorldGenerator.NREGIONS;
  }

  /**
   * Usually returns an empty set.
   *
   * @return a set of points which will be considered as already included in the
   *   generated area, before starting the {@link #generatearea(World)} process
   *   proper.
   */
  protected HashSet<Point> generatestartingarea(World world){
    return new HashSet<>();
  }

  /**
   * Decides where to start the {@link #expand(HashSet, World, Point)} process
   * from.
   *
   * @param source The very starting point for this area.
   * @param current The last expanded point for this area.
   * @return the source, by default. Subclasses may change this behavior.
   */
  protected Point generatereference(Point source,Point current){
    return source;
  }

  /**
   * @param area Given the current generated area...
   * @param path and a point of reference...
   * @return A new point to be added to the area.
   */
  protected Point expand(HashSet<Point> area,World world){
    Point result=null;
    while(result==null){
      var p=RPG.pick(area);
      for(var next:RPG.shuffle(p.getadjacent())){
        if(!checkinvalid(next.x,next.y,world)){
          result=next;
          break;
        }
        WorldGenerator.retry();
      }
    }
    return result;
  }

  /**
   * @param x Coordinate.
   * @param y Coordinate.
   * @return <code>false</code> if for any reason the given coordinate shouldn't
   *   be added to this area.
   */
  boolean checkinvalid(int x,int y,World world){
    return !World.validatecoordinate(x,y)||!generatetile(world.map[x][y],world)
        ||checktown(x,y);
  }

  boolean checktown(int x,int y){
    for(Actor town:Town.gettowns()) if(town.x==x&&town.y==y) return true;
    return false;
  }

  /**
   * @return The starting point for this area.
   */
  protected Point generatesource(World world){
    return new Point(randomaxispoint(),randomaxispoint());
  }

  /**
   * @param p Given a point...
   * @param neighbor will check if there is such a terrain tile...
   * @param radius in the given radius around it.
   * @param w World instance.
   * @return Number of terrain tiles from the given type found in radius.
   */
  public static int search(Point p,Terrain neighbor,int radius,World w){
    var found=0;
    for(var x=p.x-radius;x<=p.x+radius;x++)
      for(var y=p.y-radius;y<=p.y+radius;y++){
        if(x==p.x&&y==p.y||!World.validatecoordinate(x,y)) continue;
        if(w.map[x][y].equals(neighbor)) found+=1;
      }
    return found;
  }

  /**
   * Called at the end of the {@link #generatearea(World)} process.
   *
   * @param area The generated area.
   */
  public void generatesurroundings(HashSet<Point> area,World w){
    // nothing by default
  }

  /**
   * @param terrain Given the terrain this area is going to expand to...
   * @return <code>true</code> if OK to overried previous terrain and expand the
   *   current area into it.
   */
  protected boolean generatetile(Terrain terrain,World world){
    return Terrain.FOREST.equals(terrain);
  }

  /**
   * @return All points of this {@link World} where this terrain exists.
   */
  protected HashSet<Point> gettiles(World world){
    var area=new HashSet<Point>();
    for(var x=0;x<World.SIZE;x++) for(var y=0;y<World.SIZE;y++)
      if(world.map[x][y]==this) area.add(new Point(x,y));
    return area;
  }

  public HashSet<Point> generate(World w){
    var area=generatearea(w);
    if(liquid)
      for(Point p:new HashSet<>(area)) if(checkisolated(p,area)) area.remove(p);
    for(Point p:area) w.map[p.x][p.y]=this;
    generatesurroundings(area,w);
    return area;
  }

  boolean checkisolated(Point p,HashSet<Point> area){
    for(Point adjacent:Point.getadjacentorthogonal()){
      adjacent.x+=p.x;
      adjacent.y+=p.y;
      if(area.contains(adjacent)) return false;
    }
    return true;
  }

  static int randomaxispoint(){
    return RPG.r(1,World.SIZE-2);
  }

  /**
   * @return <code>true</code> if active {@link Squad} can enter this location.
   */
  public boolean enter(int x,int y){
    return true;
  }

  /**
   * Hazards are things like a sandstorm in a {@link Desert} or a storm in
   * {@link Water}, which can have dire implication for a {@link Squad}
   * travelling in that terrain, Somewhat of a misnomer, a hazard can also be a
   * more peaceful event like meeting a special character or such.
   *
   * Almost all types of hazards are dependent upon {@link Season},
   * {@link Weather} and day period conditions. No one suffers a heatstroke on
   * the desert during the night, for example.
   *
   * Usually, even if the conditions for multiple types of hazards are met in a
   * certain time period, only one of them should actually trigger.
   *
   * @param special <code>true</code> if may allow a special event to happen.
   *   Special events are rare occurances like a spontaneous avalanche.
   * @see #HAZARDCHANCE
   * @see Javelin#getperiod()
   * @see WorldMove
   */
  public Set<Hazard> gethazards(boolean special){
    var hazards=new HashSet<Hazard>();
    if(special) hazards.add(new Hail());
    return hazards;
  }

  /**
   * @return a string representation of the {@link Weather}.
   * @see Weather#current
   */
  public String describeweather(){
    if(Weather.current==Weather.RAIN) return "raining";
    if(Weather.current==Weather.STORM)
      return Season.current==Season.WINTER?SNOWING:"storm";
    return "";
  }

  /**
   * @param teamel Added to the encounter level delta.
   * @return Encounter level for a fight taking place in this type of terrain.
   */
  public Integer getel(int teamel){
    return teamel+Math.min(Difficulty.get(),difficultycap);
  }

  /**
   * @return All valid monsters for this terrain type.
   */
  public ArrayList<Monster> getmonsters(){
    var recruits=new ArrayList<Monster>();
    for(Monster m:Monster.ALL)
      if(m.getterrains().contains(name)) recruits.add(m);
    return recruits;
  }

  /** @return {@link Weather#current} by default. */
  public Integer getweather(){
    return Weather.current;
  }

  /** @return Terrain {@link Encounter}s. */
  public EncounterIndex getencounters(){
    return Organization.ENCOUNTERSBYTERRAIN.get(name);
  }

  /** @return {@link Fight} map. */
  public Map getmap(){
    var m=maps;
    if(Squad.active!=null&&!shoremaps.isEmpty()){
      var s=Squad.active.getlocation();
      if(Terrain.search(s,Terrain.WATER,1,World.seed)>0) m=shoremaps;
    }
    return m.pick();
  }

  /** @return Count of {@link #maps} and {@link #shoremaps}; */
  public int countmaps(){
    return maps.size()+shoremaps.size();
  }
}
