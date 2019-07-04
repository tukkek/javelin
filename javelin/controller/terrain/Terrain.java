package javelin.controller.terrain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.action.world.WorldMove;
import javelin.controller.challenge.Difficulty;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.map.Map;
import javelin.controller.map.Maps;
import javelin.controller.terrain.hazard.Hail;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Survival;
import javelin.model.world.Actor;
import javelin.model.world.Season;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
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
	 * Description return by {@link #getweather()} in case of
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
	/** Represent {@link Dungeon}s and {@link Mine}s. */
	public static final Terrain UNDERGROUND=new Underground();

	/** All terrain types. */
	public static final Terrain[] ALL=new Terrain[]{PLAIN,HILL,FOREST,MOUNTAINS,
			DESERT,MARSH,UNDERGROUND,WATER};
	/** All terrain types except {@link #WATER} and {@link #UNDERGROUND}. */
	public static final Terrain[] STANDARD=new Terrain[]{PLAIN,HILL,FOREST,
			MOUNTAINS,DESERT,MARSH};
	/** All terrain types except {@link #UNDERGROUND}. */
	public static final Terrain[] NONUNDERGROUND=new Terrain[]{PLAIN,HILL,FOREST,
			MOUNTAINS,DESERT,MARSH,WATER};
	/** All terrain types except {@link #water}. */
	public static final List<Terrain> NONWATER=List.of(PLAIN,HILL,FOREST,
			MOUNTAINS,DESERT,MARSH,UNDERGROUND);

	static final int[] STEPS=new int[]{-1,0,+1};

	/** No road. */
	public Float speedtrackless=null;
	/** Minor road. */
	public Float speedroad=null;
	/** Major road. */
	public Float speedhighway=null;
	/** Used to determine tile. */
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

	private ArrayList<Actor> towns;

	/** ASCII representation of terrain type for debugging purposes. */
	public Character representation=null;

	/** Terrains that "overflow". They receive a "shore" visual. */
	public boolean liquid=false;

	/** A bonus to be added manually to {@link Survival} rolls. */
	public int survivalbonus=0;

	/**
	 * Uses current terrain as base.
	 *
	 * @param mph Applies terrain penalty to base Squad speed.
	 * @param x {@link World} coordinate.
	 * @param y {@link World} coordinate.
	 * @return Speed in miles per hour to traverse this terrain.
	 *
	 * @see Squad#move()
	 * @see Terrain#current()
	 */
	public int speed(int mph,int x,int y){
		return Math.round(mph*getspeed(x,y));
	}

	/**
	 * @param x {@link World} coordinate.
	 * @param y {@link World} coordinate.
	 * @return A percentage value determining how fast it is to walk here, based
	 *         on road status.
	 * @see World#roads
	 * @see World#highways
	 */
	public float getspeed(int x,int y){
		if(!World.seed.roads[x][y]) return speedtrackless;
		return World.seed.highways[x][y]?speedhighway:speedroad;
	}

	/**
	 * TODO this probably should return {@link Underground} as well.
	 *
	 * @return Current terrain difficulty. For example: {@link PLAIN}.
	 */
	static public Terrain current(){
		if(JavelinApp.context==null) return null;
		Point h=JavelinApp.context.getsquadlocation();
		return h==null?null:Terrain.get(h.x,h.y);
	}

	/**
	 * @param x {@link World} coordinate.
	 * @param y {@link World} coordinate.
	 * @return {@link Underground} if there is a {@link Dungeon#active} or the
	 *         {@link World} terrain.
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

	/**
	 * @return A battle map instance to be generated.
	 * @see Map#generate()
	 */
	abstract public Maps getmaps();

	HashSet<Point> generatearea(World world){
		Point source=generatesource(world);
		Point current=source;
		HashSet<Point> area=generatestartingarea(world);
		int size=getareasize();
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
		return World.scenario.size*World.scenario.size/WorldGenerator.NREGIONS;
	}

	/**
	 * Usually returns an empty set.
	 *
	 * @return a set of points which will be considered as already included in the
	 *         generated area, before starting the {@link #generatearea(World)}
	 *         process proper.
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
	 * @param p and a point of reference...
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
	 *         be added to this area.
	 */
	boolean checkinvalid(int x,int y,World world){
		return !World.validatecoordinate(x,y)||!generatetile(world.map[x][y],world)
				||checktown(x,y);
	}

	boolean checktown(int x,int y){
		for(Actor town:Town.gettowns())
			if(town.x==x&&town.y==y) return true;
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
	 * @param World World instance.
	 * @return Number of terrain tiles from the given type found in radius.
	 */
	public static int search(Point p,Terrain neighbor,int radius,World w){
		int found=0;
		for(int x=p.x-radius;x<=p.x+radius;x++)
			for(int y=p.y-radius;y<=p.y+radius;y++){
				if(x==p.x&&y==p.y) continue;
				if(!World.validatecoordinate(x,y)) continue;
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
	 *         current area into it.
	 */
	protected boolean generatetile(Terrain terrain,World world){
		return Terrain.FOREST.equals(terrain);
	}

	/**
	 * @return All points of this {@link World} where this terrain exists.
	 */
	protected HashSet<Point> gettiles(World world){
		HashSet<Point> area=new HashSet<>();
		for(int x=0;x<World.scenario.size;x++)
			for(int y=0;y<World.scenario.size;y++)
				if(world.map[x][y]==this) area.add(new Point(x,y));
		return area;
	}

	public HashSet<Point> generate(World w){
		HashSet<Point> area=generatearea(w);
		if(liquid) for(Point p:new HashSet<>(area))
			if(checkisolated(p,area)) area.remove(p);
		for(Point p:area)
			w.map[p.x][p.y]=this;
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
		return RPG.r(1,World.scenario.size-2);
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
	 *          Special events are rare occurances like a spontaneous avalanche.
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
	public String getweather(){
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
		ArrayList<Monster> recruits=new ArrayList<>();
		for(Monster m:Monster.MONSTERS)
			if(m.getterrains().contains(name)) recruits.add(m);
		return recruits;
	}
}
