package javelin.controller.map;

import java.awt.Image;
import java.util.ArrayList;

import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.ai.BattleAi;
import javelin.controller.map.terrain.Water;
import javelin.controller.terrain.Terrain;
import javelin.model.state.Square;
import javelin.model.unit.Monster;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * Representation of a battle area.
 *
 * @author alex
 */
public abstract class Map {
	/** Background image. */
	public Image floor = Images.getImage("terraindirt");
	/** Represents {@link Square#blocked}. */
	public Image wall = Images.getImage("terrainwall");
	/**
	 * If given, instead of using {@link #wall} on top of a {@link #floor}
	 * bakcground will use this instead.
	 */
	public Image wallfloor = null;
	/** Represents an {@link Square#obstructed} item. */
	public Image obstacle = Images.getImage("terrainbush");
	/** Used often as an {@link #obstacle}. */
	public Image rock = Images.getImage("terrainrock");
	/** Represents {@link Square#flooded}. */
	public Image flooded = Images.getImage("terrainflooded");

	/**
	 * Two-dimensional array of squares. map[point.x][point.y]
	 */
	public Square[][] map;
	/** See {@link Weather}. By default allows any extent of flooding. */
	public int maxflooding = Weather.STORM;

	/** Map title. */
	public String name;

	/**
	 * Maps that are supposed to be good for any minigame or situation.
	 *
	 * @see #random()
	 */
	public boolean standard = true;
	/**
	 * Possible starting positions for the player team. If empty, will be
	 * ignored.
	 */
	public ArrayList<Point> startingareablue = new ArrayList<Point>(0);
	/** Same as {@link #startingareablue} but for enemies. */
	public ArrayList<Point> startingareared = new ArrayList<Point>(0);

	/**
	 * Usually <code>true</code> but confined spaces where flyers cannot fly
	 * over walls will be <code>false</code>.
	 *
	 * This is done for consistency but mostly because it allows for the
	 * {@link BattleAi} to just stay out of reach with flying creatures instead
	 * of losing a fight. {@link Dungeon} maps for example can have wall
	 * placement that makes it very hard to kill a flying unit unless you have
	 * one yourself.
	 *
	 * @see Monster#fly
	 */
	public boolean flying = true;

	/**
	 * Construcor based on map size. By default all {@link Square}s are
	 * completely free.
	 */
	public Map(String namep, int width, int height) {
		name = namep;
		map = new Square[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				map[x][y] = new Square(false, false, false);
			}
		}
	}

	/**
	 * Creates the {@link #map}. Called after construction to allow for fast
	 * instantiation of a {@link Maps} list and lazy generation operation.
	 */
	abstract public void generate();

	/** Marks {@link Square#flooded}. */
	public void putwater(int x, int y) {
		map[x][y].flooded = true;
	}

	/** Marks {@link Square#obstructed}. */
	final public void putobstacle(int x, int y) {
		map[x][y].obstructed = true;
	}

	/**
	 * @return {@link #obstacle} representation for this map. May be overridden
	 *         for dynamic purposes.
	 */
	public Image getobstacle() {
		return obstacle;
	}

	/** Marks {@link Square#blocked}. */
	final public void putwall(int x, int y) {
		map[x][y].blocked = true;
	}

	/**
	 * @return Image that represents {@link Square#blocked}.
	 * @see #wall
	 * @see #wallfloor
	 */
	public Image getblockedtile(int x, int y) {
		return wallfloor == null ? wall : wallfloor;
	}

	/**
	 * @return <code>false</code> if this map can't be used now due to any
	 *         circumstances.
	 */
	public boolean validate() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		return name.equals(((Map) obj).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public boolean validatecoordinate(int x, int y) {
		return 0 <= Math.min(x, y) && Math.max(x, y) < map.length;
	}

	/**
	 * @return A random map, excluding ones that are usually not recommended for
	 *         typical fights like {@link Water}.
	 */
	public static Map random() {
		Terrain t = Terrain.NONWATER[RPG.r(0, Terrain.NONWATER.length - 1)];
		return RPG.pick(t.getmaps());
	}

	@Override
	public String toString() {
		String map = "";
		for (int x = 0; x < this.map.length; x++) {
			for (int y = 0; y < this.map[x].length; y++) {
				Square s = this.map[x][y];
				if (s.blocked) {
					map += '#';
				} else if (s.obstructed) {
					map += '.';
				} else if (s.flooded) {
					map += '~';
				} else {
					map += ' ';
				}
			}
			map += "\n";
		}
		return map;
	}
}
