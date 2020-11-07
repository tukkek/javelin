package javelin.controller.map;

import java.awt.Image;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.ai.BattleAi;
import javelin.controller.map.terrain.water.DeepWaters;
import javelin.controller.terrain.Terrain;
import javelin.model.state.Square;
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
	 * losing a fight. {@link DungeonFloor} maps for example can have wall placement
	 * that makes it very hard to kill a flying unit unless you have one yourself.
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
		for(int x=0;x<width;x++)
			for(int y=0;y<height;y++)
				map[x][y]=new Square();
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
	 *         for dynamic purposes.
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
	 *         circumstances.
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

	public boolean validate(int x,int y){
		return 0<=x&&x<map.length&&0<=y&&y<map[0].length;
	}

	/**
	 * @return A random map, excluding ones that are usually not recommended for
	 *         typical fights like {@link DeepWaters}.
	 */
	public static Map random(){
		return RPG.pick(RPG.pick(Terrain.NONWATER).getmaps());
	}

	@Override
	public String toString(){
		String map="";
		for(Square[] element:this.map){
			for(Square s:element)
				if(s.blocked)
					map+='#';
				else if(s.obstructed)
					map+='.';
				else if(s.flooded)
					map+='~';
				else
					map+=' ';
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
		for(int x=0;x<width;x++){
			map[x][0].blocked=true;
			border.add(new Point(x,0));
			map[x][width-1].blocked=true;
			border.add(new Point(x,width-1));
		}
		for(int y=0;y<height;y++){
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
		while(p==null||map[p.x][p.y].blocked)
			p=getrandompoint();
		return p;
	}

	/** @return Total map area in tiles (width times height). */
	protected int getarea(){
		return map.length*map[0].length;
	}

	/** @return All {@link Terrain#NONWATER} maps. */
	public static List<Map> getall(){
		return Terrain.NONWATER.stream().map(t->t.getmaps())
				.flatMap(ms->ms.stream()).collect(Collectors.toList());
	}

	/** @return Floor image for the given coordinate (usually {@link #floor}). */
	public Image getfloor(int x,int y){
		return floor;
	}
}
