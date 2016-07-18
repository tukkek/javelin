package javelin.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.BattleSetup;
import javelin.controller.Point;
import javelin.controller.ai.Node;
import javelin.controller.old.Game;
import javelin.controller.terrain.map.Map;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.BaseObject;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.engine.ThingOwner;
import tyrant.mikera.tyrant.Being;
import tyrant.mikera.tyrant.Damage;
import tyrant.mikera.tyrant.Event;
import tyrant.mikera.tyrant.Portal;
import tyrant.mikera.tyrant.Spell;
import tyrant.mikera.tyrant.Theme;
import tyrant.mikera.tyrant.Tile;
import tyrant.mikera.tyrant.WorldMap;
import tyrant.mikera.tyrant.test.MapHelper;

/**
 * The root class for all maps in the tyrant universe
 * 
 * Map contains a wide variety of functions for creating and drawing map
 * elements, and also for managing the set of all objects placed on the map
 * 
 * All specific types of map (dungeons, towns etc) should extend Map
 * 
 * Some particularly useful Map creation functions: setTile copyArea fillArea
 * fillBorder rotateArea
 * 
 * Particularly useful object management functions: addThing getThings
 * 
 * Some useful AI helper functions getMobile getNearbyMobile countNearbyMobiles
 * findNearestFoe
 * 
 * Note: Map is final for performance reasons
 * 
 */

public class BattleMap extends BaseObject implements ThingOwner {
	private static final int[] DELTAS = new int[] { -1, 0, +1 };

	public static class VisionCache {
		public static ArrayList<Point> positions = new ArrayList<Point>();

		public String period;
		final public int vision;
		public ArrayList<Point> seen = new ArrayList<Point>();

		public VisionCache(final String perception, final Thing h) {
			period = perception;
			vision = h.combatant.source.vision;
		}

		@Override
		public boolean equals(Object obj) {
			final VisionCache that = (VisionCache) obj;
			return that.period.equals(that.period) && vision == that.vision;
		}

		static ArrayList<Point> pointsfromcombatents() {
			ArrayList<Point> points =
					new ArrayList<Point>(BattleMap.combatants.size());
			for (Combatant c : BattleMap.combatants) {
				points.add(new Point(c.location[0], c.location[1]));
			}
			return points;
		}

		static boolean valid() {
			ArrayList<Point> points = pointsfromcombatents();
			if (positions.equals(points)) {
				return true;
			}
			positions = points;
			return false;
		}
	}

	private static final long serialVersionUID = 2476911722567644463L;
	// Map storage
	private int[] tiles;
	private Thing[] objects;
	public int width;
	public int height;
	private int size;
	private Game game;

	// pathfinding temporary sorage
	// low byte = dist to hero
	public transient int[] path;
	public String period;
	public ArrayList<Meld> meld = new ArrayList<Meld>();
	/** Mapping of vision cache data by {@link Combatant#id}. */
	public static final TreeMap<Integer, VisionCache> visioncache =
			new TreeMap<Integer, VisionCache>();

	// Internal constants
	private static final int LOS_DETAIL = 26;
	private static final double DLOS_DETAIL = BattleMap.LOS_DETAIL;

	// Direction stuff
	public static final int DIR_NONE = 0;
	public static final int DIR_N = 1;
	public static final int DIR_NE = 2;
	public static final int DIR_E = 3;
	public static final int DIR_SE = 4;
	public static final int DIR_S = 5;
	public static final int DIR_SW = 6;
	public static final int DIR_W = 7;
	public static final int DIR_NW = 8;

	public static final int[] DX = { 0, 0, 1, 1, 1, 0, -1, -1, -1 };
	public static final int[] DY = { 0, -1, -1, 0, 1, 1, 1, 0, -1 };

	public static ArrayList<Combatant> combatants = new ArrayList<Combatant>();
	public static ArrayList<Combatant> redTeam = new ArrayList<Combatant>();
	public static ArrayList<Combatant> blueTeam = new ArrayList<Combatant>();
	public static ArrayList<Combatant> dead = new ArrayList<Combatant>();
	public static Boolean victory = null;

	public BattleMap(final int w, final int h) {
		// game=Game.instance();

		// allocate arrays
		size = w * h;
		tiles = new int[size];
		path = new int[size];
		objects = new Thing[size];
		width = w;
		height = h;

		set("HashName", "unknown");
		set("FloorTile", Tile.FLOOR);
		set("WallTile", Tile.CAVEWALL);
		set("Description", "Unknown Area");
		set("Level", 1);
		set("MonsterType", "IsHostile");
		set("WanderingRate", 100);
		// set("IsHostile",1);
	}

	public Thing entrance() {
		return getEntrance();
	}

	public void setTheme(final String s) {
		setTheme(Theme.getTheme(s));
	}

	/**
	 * set the theme for the map
	 */
	public void setTheme(final Theme t) {
		final java.util.Map h = t.getCollapsedMap();
		final Iterator it = h.keySet().iterator();
		while (it.hasNext()) {
			final String key = (String) it.next();
			if (!key.equals("Name")) {
				set(key, h.get(key));
			}
		}
		set("Theme", t.getString("Name"));
	}

	public int wall() {
		return getStat("WallTile");
	}

	public int floor() {
		return getStat("FloorTile");
	}

	/**
	 * set size (resets everything to blank)
	 */
	public void setSize(final int w, final int h) {
		size = w * h;
		tiles = new int[size];
		path = new int[size];
		objects = new Thing[size];
		width = w;
		height = h;
	}

	/**
	 * checks if hero can exit from current location
	 */
	public boolean canExit() {
		return false;
	}

	public void setAngry(final boolean a) {
		final boolean alreadyAngry = getFlag("IsHostile");
		if (!alreadyAngry) {
			// Game.message("You hear angry shouts from all directions");
			set("IsHostile", a);
		}
	}

	public boolean isAngry() {
		return getFlag("IsHostile");
	}

	public void clearPath() {
		// clear the path grid
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				path[x + y * width] = 0;
			}
		}
	}

	public void calcPath(final int px, final int py, final int distance) {
		// clear the path grid
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				path[x + y * width] = -1;
			}
		}

		path[px + py * height] = 0;

		// calculate distance for all surrounding squares
		for (int i = 1; i < distance; i++) {
			final int x1 = RPG.max(1, px - i);
			final int y1 = RPG.max(1, py - i);
			final int x2 = RPG.min(width - 2, px + i);
			final int y2 = RPG.min(width - 2, py + i);

			for (int x = x1; x <= x2; x++) {
				for (int y = y1; y <= y2; y++) {
					final int pos = x + y * width;
					if (

					path[pos] == -1

							&& (getTile(x, y) & Tile.TF_BLOCKED) == 0

							&& (path[pos - width - 1] == i
									|| path[pos - width] == i
									|| path[pos - width + 1] == i
									|| path[pos - 1] == i || path[pos + 1] == i
									|| path[pos + width - 1] == i
									|| path[pos + width] == i
									|| path[pos + width + 1] == i)) {
						path[pos] = i + 1;
					}
				}
			}
		}

		final int x1 = RPG.max(1, px - distance);
		final int y1 = RPG.max(1, py - distance);
		final int x2 = RPG.min(width - 2, px + distance);
		final int y2 = RPG.min(width - 2, py + distance);

		// now fill in individual square direction pointers
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				final int pos = x + y * width;
				final int v = path[pos];
				if (v > 0) {
					for (int i = 1; i <= 8; i++) {
						if (path[pos + BattleMap.DX[i]
								+ width * BattleMap.DY[i]] == v - 1) {
							setTile(x, y, getTileFull(x, y) & ~Tile.TF_DIRECTION
									| i * Tile.TF_DIRECTIONBASE);
							continue;
						}
					}
				}
			}
		}
	}

	/**
	 * fractally builds an area - square technique. very cool algorithm!!
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param gran
	 *            must be a power of 2
	 */
	public void fractalize(final int x1, final int y1, int x2, int y2,
			final int gran) {
		// ensure workable size
		x2 = x1 + (x2 - x1) / gran * gran - 1;
		y2 = y1 + (y2 - y1) / gran * gran - 1;

		final int g = gran / 2;
		if (g < 1) {
			return;
		}
		for (int y = y1; y <= y2; y += gran) {
			for (int x = x1; x <= x2; x += gran) {
				if (RPG.r(2) == 0) {
					setTile(x + g, y, getTileFull(x, y));
				} else {
					setTile(x + g, y, getTileFull(x + gran, y));
				}
				if (RPG.r(2) == 0) {
					setTile(x, y + g, getTileFull(x, y));
				} else {
					setTile(x, y + g, getTileFull(x, y + gran));
				}
			}
		}

		// now do middle tile
		for (int y = y1; y <= y2; y += gran) {
			for (int x = x1; x <= x2; x += gran) {
				int c;
				switch (RPG.d(4)) {
				case 1:
					c = getTileFull(x + g, y);
					break;
				case 2:
					c = getTileFull(x + g, y + gran);
					break;
				case 3:
					c = getTileFull(x, y + g);
					break;
				default:
					c = getTileFull(x + gran, y + g);
					break;
				}
				setTile(x + g, y + g, c);
			}
		}

		// continue down to next level of detail
		if (g > 1) {
			fractalize(x1, y1, x2, y2, g);
		}
	}

	/**
	 * fractally builds an area - wrapping block technique areas are NOT
	 * guaranteed to be connected but looks prettier than vanilla fractalize
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param gran
	 *            must be a power of 2
	 */
	public void fractalizeBlock(final int x1, final int y1, final int x2,
			final int y2, final int gran) {
		final int g = gran / 2;
		final int mask = ~(gran - 1);
		if (g < 1) {
			return;
		}
		for (int y = y1; y <= y2; y += g) {
			for (int x = x1; x <= x2; x += g) {
				int tx = RPG.r(2) == 0 ? x : x + g;
				int ty = RPG.r(2) == 0 ? y : y + g;
				tx = (tx - x1 & mask) + x1;
				ty = (ty - y1 & mask) + y1;
				setTile(x, y, getTileFull(tx, ty));
			}
		}
		if (g > 1) {
			fractalizeBlock(x1, y1, x2, y2, g);
		}
	}

	public void calcReachable(final int px, final int py) {
		// clear the path grid
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				path[x + y * width] = 0;
			}
		}

		path[px + py * height] = 1;

		boolean found = true;

		int pass = 1;
		while (found) {
			pass++;
			found = false;
			for (int x = 1; x < width - 1; x++) {
				for (int y = 1; y < height - 1; y++) {
					if (path[x + y * width] == 0

							&& (getTile(x, y) & Tile.TF_BLOCKED) == 0

							&& (path[x + (y - 1) * width] == pass
									|| path[x + 1 + (y - 1) * width] == pass
									|| path[x + 1 + y * width] == pass
									|| path[x + 1 + (y + 1) * width] == pass
									|| path[x + (y + 1) * width] == pass
									|| path[x - 1 + (y + 1) * width] == pass
									|| path[x - 1 + y * width] == pass
									|| path[x - 1 + (y - 1) * width] == pass)) {
						path[x + y * width] = pass;
						found = true;
					}
				}
			}
		}
	}

	/** replace all tiles of a certain type */
	public void replaceTiles(final int from, final int to) {
		replaceTiles(0, 0, width - 1, height - 1, from, to);
	}

	/** replace all tiles of a certain type */
	public void replaceTiles(final int x1, final int y1, final int x2,
			final int y2, final int from, final int to) {
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				if (getTile(x, y) == from) {
					setTile(x, y, to);
				}
			}
		}
	}

	public boolean isBlank(final int x, final int y) {
		// return getTile(x, y) == 0;
		return false;
	}

	/** check if area is completely empty */
	public boolean isBlank(int x1, int y1, int x2, int y2) {
		// get right order for co-ordinates
		if (x1 > x2) {
			final int t = x1;
			x1 = x2;
			x2 = t;
		}
		if (y1 > y2) {
			final int t = y1;
			y1 = y2;
			y2 = t;
		}

		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				if (getTile(x, y) != 0) {
					return false;
				}
			}
		}
		return true;
	}

	public void setLevel(final int level) {
		set("Level", level);
	}

	/** get level of the map */
	public int getLevel() {
		return getStat("Level");
	}

	/** count tiles of particular type in rectangular area */
	public int countTiles(final int x1, final int y1, final int x2,
			final int y2, final int c) {
		int count = 0;
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				if (getTile(x, y) == c) {
					count++;
				}
			}
		}
		return count;
	}

	/** fills rectangular map area with specified tile */
	public void fillArea(int x1, int y1, int x2, int y2, final int c) {
		if (x1 > x2) {
			final int t = x1;
			x1 = x2;
			x2 = t;
		}
		if (y1 > y2) {
			final int t = y1;
			y1 = y2;
			y2 = t;
		}
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				setTile(x, y, c);
			}
		}
	}

	/**
	 * "Spray" a tile type over an area, starting at centre and ensuring NSEW
	 * connectedness.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param c
	 *            Tile type to use
	 * @param density
	 *            Upper bound on percentage of area to cover
	 */
	public void spray(final int x1, final int y1, final int x2, final int y2,
			final int c, final int density) {
		final int cx = (x2 + y1) / 2;
		final int cy = (y2 + y1) / 2;
		setTile(cx, cy, c);

		final int w = x2 - x1 + 1;
		final int h = y2 - y1 + 1;
		for (int i = (w + h) * density / 100; i > 0; i--) {
			final int x = RPG.rspread(x1, x2);
			final int y = RPG.rspread(y1, y2);

			makeRandomPath(cx, cy, x, y, x1, y1, x2, y2, c, false);
		}
	}

	/** Fills oval map area with specified tile */
	public void fillOval(int x1, int y1, int x2, int y2, final int c) {
		if (x1 > x2) {
			final int t = x1;
			x1 = x2;
			x2 = t;
		}
		if (y1 > y2) {
			final int t = y1;
			y1 = y2;
			y2 = t;
		}
		final double cx = (x1 + x2) / 2.0;
		final double cy = (y1 + y2) / 2.0;
		final double cw = (cx - x1) * 1.005;
		final double ch = (cy - y1) * 1.005;
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				if ((x - cx) * (x - cx) / (cw * cw)
						+ (y - cy) * (y - cy) / (ch * ch) < 1) {
					setTile(x, y, c);
				}
			}
		}
	}

	public void fillRoom(final int x1, final int y1, final int x2, final int y2,
			final int wall, final int floor) {
		fillArea(x1 + 1, y1 + 1, x2 - 1, y2 - 1, floor);
		fillBorder(x1, y1, x2, y2, wall);
	}

	/**
	 * clears all objects from area use with EXTREME caution don't kill portals,
	 * secret doors, artifacts etc!!!
	 */
	public void clearArea(final int x1, final int y1, final int x2,
			final int y2) {
		final Thing[] things = getThings(x1, y1, x2, y2);
		for (final Thing thing : things) {
			thing.remove();
		}
	}

	/** clear area and fill with specified tile */
	public void clearArea(final int x1, final int y1, final int x2,
			final int y2, final int c) {
		clearArea(x1, y1, x2, y2);
		fillArea(x1, y1, x2, y2, c);
	}

	/**
	 * spread src tiles over dst tiles do this by filling adjacent squares
	 */
	public void spreadTiles(final int x1, final int y1, final int x2,
			final int y2, final int src, final int des) {
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				if (getTile(x, y) == des) {
					for (int dx = -1; dx <= 1; dx++) {
						for (int dy = -1; dy <= 1; dy++) {
							if (getTile(x + dx, y + dy) == src) {
								setTile(x, y, 65535);
							}
						}
					}
				}
			}
		}
		replaceTiles(65535, src);
	}

	/**
	 * randomly shakes tiles in area to blur outlines useful to create irregular
	 * patterns
	 */
	public void blurArea(final int x1, final int y1, final int x2,
			final int y2) {
		for (int i = 0; i < (x2 - x1 + 1) * (y2 - y1 + 1) / 4; i++) {
			final int sx = RPG.rspread(x1 + 1, x2 - 1);
			final int sy = RPG.rspread(y1 + 1, y2 - 1);
			final int dx = sx + RPG.r(3) - 1;
			final int dy = sy + RPG.r(3) - 1;
			final int t = getTileFull(sx, sy);
			setTile(sx, sx, getTileFull(dx, dy));
			setTile(dx, dy, t);
		}
	}

	/**
	 * rotates a square map region by <count> right angles rotates in a
	 * clockwize direction
	 */
	public void rotateArea(final int x, final int y, final int size,
			int count) {
		// make rotation in range 0-3
		count &= 3;

		// bail out or recurse as needed
		if (count == 0) {
			return;
		}
		if (count > 1) {
			rotateArea(x, y, size, count - 1);
		}

		// rotate tiles
		for (int p = 0; p < (size + 1) / 2; p++) {
			for (int q = 0; q < size / 2; q++) {
				final int temp = getTileFull(x + p, y + q);
				setTile(x + p, y + q, getTileFull(x + q, y + size - 1 - p));
				setTile(x + q, y + size - 1 - p,
						getTileFull(x + size - 1 - p, y + size - 1 - q));
				setTile(x + size - 1 - p, y + size - 1 - q,
						getTileFull(x + size - 1 - q, y + p));
				setTile(x + size - 1 - q, y + p, temp);
			}
		}

		// rotate map objects
		final Thing[] things = getThings(x, y, x + size - 1, y + size - 1);
		for (final Thing t : things) {
			final int nx = x + size - 1 - (t.y - y);
			final int ny = y + t.x - x;
			addThing(t, nx, ny);
		}
	}

	/** copy a complte map template */
	public void copyArea(final int tx, final int ty, final BattleMap src) {
		copyArea(tx, ty, src, 0, 0, src.width, src.height);
	}

	/** copy map contents to specified position */
	public void copyArea(final int tx, final int ty, final BattleMap src,
			final int sx, final int sy, final int sw, final int sh) {
		// delete original contents
		clearArea(tx, ty, tx + sx, ty + sy);

		// square-by-square copy
		for (int x = tx; x < tx + sw; x++) {
			for (int y = ty; y < ty + sh; y++) {
				setTile(x, y, src.getTileFull(x - tx + sx, y - ty + sy));
				final Thing[] things = src.getThings(x - tx + sx, y - ty + sy);
				for (final Thing thing : things) {
					addThing((Thing) thing.clone(), x, y);
				}
			}
		}
	}

	/** creates a border around specified rectangular area */
	public void fillBorder(int x1, final int y1, int x2, int y2, final int c) {
		if (x1 > x2) {
			final int t = x1;
			x1 = x2;
			x2 = t;
		}
		if (y1 > y2) {
			final int t = y1;
			x1 = y2;
			y2 = t;
		}
		for (int x = x1; x <= x2; x++) {
			setTile(x, y1, c);
			setTile(x, y2, c);
		}
		for (int y = y1; y <= y2; y++) {
			setTile(x1, y, c);
			setTile(x2, y, c);
		}
	}

	/** fill all blank squares in area with specified tile */
	public void completeArea(int x1, final int y1, int x2, int y2,
			final int c) {
		if (x1 > x2) {
			final int t = x1;
			x1 = x2;
			x2 = t;
		}
		if (y1 > y2) {
			final int t = y1;
			x1 = y2;
			y2 = t;
		}
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				if (getTile(x, y) == 0) {
					setTile(x, y, c);
				}
			}
		}
	}

	/** set tile and return true if currently blank */
	public boolean completeTile(final int x, final int y, final int c) {
		if (getTile(x, y) == 0) {
			setTile(x, y, c);
			return true;
		}
		return false;
	}

	/**
	 * make a random path from (x1,y1) to (x2,y2) staying within region
	 * (x3,y3,x4,y4)
	 */
	public void makeRandomPath(int x1, int y1, final int x2, final int y2,
			final int x3, final int y3, final int x4, final int y4, final int c,
			final boolean diagonals) {
		int dx;
		int dy;
		while (x1 != x2 || y1 != y2) {
			setTile(x1, y1, c);
			// for (int i = 0; i < 10; i++) {
			// setTile(x1 + RPG.pick(DELTAS), y1 + RPG.pick(DELTAS), c);
			// }
			if (RPG.d(3) == 1) {
				dx = RPG.sign(x2 - x1);
				dy = RPG.sign(y2 - y1);
			} else {
				dx = RPG.r(3) - 1;
				dy = RPG.r(3) - 1;
			}
			switch (RPG.d(diagonals ? 3 : 2)) {
			case 1:
				dx = 0;
				break;
			case 2:
				dy = 0;
				break;
			}
			x1 += dx;
			y1 += dy;
			x1 = RPG.middle(x3, x1, x4);
			y1 = RPG.middle(y3, y1, y4);
		}
		setTile(x2, y2, c);
	}

	public Point findClearTile() {
		int x;
		int y;
		for (int i = 0; i < width * height; i++) {
			x = RPG.r(width);
			y = RPG.r(height);
			if (isClear(x, y)) {
				return new Point(x, y);
			}
		}
		return null;
	}

	public Point findTile(int c) {
		int x;
		int y;
		c = c & Tile.TF_TYPEMASK; // use tile type for comparison
		for (int i = 0; i < 10 * width * height; i++) {
			x = RPG.r(width);
			y = RPG.r(height);
			if ((getTile(x, y) & Tile.TF_TYPEMASK) == c) {
				return new Point(x, y);
			}
		}
		return null;
	}

	/** Finds the first instance of a named thing on the map */
	public Thing find(final String name) {
		return find(name, 0, 0, width - 1, height - 1);
	}

	/** Finds the first instance of a named thing in a given area */
	public Thing find(final String name, final int x1, final int y1,
			final int x2, final int y2) {
		for (int y = y1; y <= y2; y++) {
			for (int x = x1; x <= x2; x++) {
				Thing tracker = objects[x + y * width];
				while (tracker != null) {
					if (tracker.name().equals(name)) {
						return tracker;
					}
					tracker = tracker.next;
				}
			}
		}
		return null;
	}

	/** find a free area, returning top left point */
	public Point findFreeArea(final int x1, final int y1, final int x2,
			final int y2, final int w, final int h) {
		int px = x1;
		int py = y1;
		for (int i = (x2 - x1) * (y2 - y1); i > 0; i--) {
			px = RPG.rspread(x1, x2 - w + 1);
			py = RPG.rspread(y1, y2 - h + 1);

			for (int lx = px; lx < px + w; lx++) {
				for (int ly = py; ly < py + h; ly++) {
					if (!isClear(lx, ly)) {
						continue;
					}
				}
			}
			return new Point(px, py);
		}
		return null;
	}

	/**
	 * find a random square in rectangular area which is: a) not zero b) not
	 * blocked c) contains no stuff
	 */
	public Point findFreeSquare(final int x1, final int y1, final int x2,
			final int y2) {
		int x;
		int y;
		// prefer completely free squares
		for (int i = 5 * (x2 - x1 + 2) * (y2 - y1 + 2); i > 0; i--) {
			x = RPG.rspread(x1, x2);
			y = RPG.rspread(y1, y2);
			final int tile = getTile(x, y);
			if (tile == 0) {
				continue;
			}
			if (getObjects(x, y) == null && Tile.isPassable(tile)) {
				return new Point(x, y);
			}
		}
		// make do with unblocked squares
		for (int i = 2 * (x2 - x1 + 2) * (y2 - y1 + 2); i > 0; i--) {
			x = RPG.rspread(x1, x2);
			y = RPG.rspread(y1, y2);
			if (getTile(x, y) == 0) {
				continue;
			}
			if (!isBlocked(x, y)) {
				return new Point(x, y);
			}
		}
		return null;
	}

	/** find free square on entire map */
	public Point findFreeSquare() {
		return findFreeSquare(0, 0, width - 1, height - 1);
	}

	/**
	 * find unblocked square with tile type c adjacent in specified direction
	 */
	public Point findEdgeSquare(final int dx, final int dy, final int c) {
		int x;
		int y;
		for (int i = 0; i < 10 * width * height; i++) {
			x = RPG.r(width - 2) + 1;
			y = RPG.r(height - 2) + 1;
			if (getTile(x, y) == 0 || isBlocked(x, y)) {
				continue;
			}
			if (getTile(x + dx, y + dy) == c) {
				return new Point(x, y);
			}
		}
		return null;
	}

	public static final int FILTER_MONSTER = 1;
	public static final int FILTER_ITEM = 2;

	// public List findStuff(final Thing b, final int filter) {
	// if (b.place != this) {
	// return null;
	// }
	// final int sx = b.x;
	// final int sy = b.y;
	// final int viewRange = Being.calcViewRange(b);
	// List l = new LinkedList();
	//
	// for (int i = 1; i <= viewRange; i++) {
	// final int x1 = sx - i;
	// final int x2 = sx + i;
	// final int y1 = sy - i;
	// final int y2 = sy + i;
	// for (int p = -i; p < i; p++) {
	// if (y2 < height && sx + p < width && sx + p >= 0) {
	// l = findStuffPoint(sx + p + y2 * width, b, filter, l);
	// }
	// if (y1 >= 0 && sx - p < width && sx - p >= 0) {
	// l = findStuffPoint(sx - p + y1 * width, b, filter, l);
	// }
	// if (x2 < width && sy + p < height && sy + p >= 0) {
	// l = findStuffPoint(x2 + (sy + p) * width, b, filter, l);
	// }
	// if (x1 >= 0 && sy - p < height && sy - p >= 0) {
	// l = findStuffPoint(x1 + (sy - p) * width, b, filter, l);
	// }
	// }
	// }
	// return l;
	// }

	/**
	 * i = location b = being ( for LOS purposes ) filter = what to look for
	 * LinkedList = list of interesting points
	 */
	public List findStuffPoint(final int i, final Thing b, final int filter,
			final List l) {
		Thing mob;
		Thing thing;
		mob = getMobileChecked(i);
		thing = getFlaggedObject(i, "ValueBase");

		if ((filter & BattleMap.FILTER_MONSTER) == BattleMap.FILTER_MONSTER) {

			if (mob != null && b.canSee(mob)) {
				l.add(new tyrant.mikera.engine.Point(mob));
				return l;
			}
		}
		if ((filter & BattleMap.FILTER_ITEM) == BattleMap.FILTER_ITEM) {
			if (thing != null && b.canSee(thing)) {
				l.add(new tyrant.mikera.engine.Point(thing));
				return l;
			}
		}
		return l;
	}

	/** find nearest enemy for a given mobile */
	public Thing findNearestFoe(final Thing b) {
		if (b.place != this) {
			return null;
		}

		final int sx = b.x;
		final int sy = b.y;
		Thing mob;

		final int viewRange = Being.calcViewRange(b);

		for (int i = 1; i <= viewRange; i++) {
			final int x1 = sx - i;
			final int x2 = sx + i;
			final int y1 = sy - i;
			final int y2 = sy + i;
			for (int p = -i; p < i; p++) {
				if (y2 < height && sx + p < width && sx + p >= 0) {
					mob = getMobileChecked(sx + p + y2 * width);
					if (mob != null && b.isHostile(mob) && b.canSee(mob)) {
						return mob;
					}
				}
				if (y1 >= 0 && sx - p < width && sx - p >= 0) {
					mob = getMobileChecked(sx - p + y1 * width);
					if (mob != null && b.isHostile(mob) && b.canSee(mob)) {
						return mob;
					}
				}
				if (x2 < width && sy + p < height && sy + p >= 0) {
					mob = getMobileChecked(x2 + (sy + p) * width);
					if (mob != null && b.isHostile(mob) && b.canSee(mob)) {
						return mob;
					}
				}
				if (x1 >= 0 && sy - p < height && sy - p >= 0) {
					mob = getMobileChecked(x1 + (sy - p) * width);
					if (mob != null && b.isHostile(mob) && b.canSee(mob)) {
						return mob;
					}
				}
			}
		}
		return null;
	}

	/**
	 * return movement cost for entering square 100=normal, 150=slowed,
	 * 300=difficult
	 */
	public int getMoveCost(final int x, final int y) {
		// return Tile.getMoveCost(getTile(x, y));
		return 0;
	}

	/**
	 * add thing to map at given location make this final for performance
	 * reasons.....
	 */
	public final Thing addThing(final Thing thing, final int x, final int y) {
		if (thing == null) {
			return null;
		}

		// hack for spell effects!
		if (thing.getFlag("IsSpell")) {
			Spell.castAtLocation(thing, null, this, x, y);
			return null;
		}

		thing.remove();
		if (thing.getFlag("NoStack")
				&& getNamedObject(x, y, thing.getString("Name")) != null) {
			return null;
		}

		// if (Game.debug&&isTileBlocked(x,y)&&(!thing.getFlag("IsSecret")))
		// throw new Error("adding thing to blocked square");
		return addObject(thing, x, y);
	}

	public final Thing addThingWithLevel(final String s, final int x,
			final int y, final int level) {
		Thing ret = null;
		final String[] ss = s.split(",");
		for (final String element : ss) {
			final String st = element.trim();
			Thing t;
			if (st.charAt(0) == '[') {
				t = Lib.createType(st.substring(1, st.length() - 1), level);
			} else {
				t = Lib.create(st, level);
			}
			ret = addThing(t, x, y);
		}
		return ret;
	}

	public final Thing addThing(final String s, final int x, final int y) {
		return addThingWithLevel(s, x, y, getLevel());
	}

	public final Thing addThing(final String s, final int x1, final int y1,
			final int x2, final int y2) {
		Thing ret = null;
		final String[] ss = s.split(",");
		for (final String element : ss) {
			ret = addThing(Lib.create(element), x1, y1, x2, y2);
		}
		return ret;
	}

	/** add thing to map in random location */
	public final Thing addThing(final Thing thing) {
		return addThing(thing, 0, 0, width - 1, height - 1);
	}

	/** add thing in random location in given area */
	public final Thing addThing(final Thing thing, final int x1, final int y1,
			final int x2, final int y2) {
		final Point p = findFreeSquare(x1, y1, x2, y2);
		if (p != null) {
			return addThing(thing, p.x, p.y);
		}
		return addThing(thing, RPG.rspread(x1, x2), RPG.rspread(y1, y2));

	}

	/**
	 * add thing in random location in given area guarantees non-blocked square
	 */
	public final boolean addBlockingThing(final Thing thing, final int x1,
			final int y1) {
		return addBlockingThing(thing, x1, y1, x1, y1);
	}

	public final boolean addBlockingThing(final Thing thing, final int x1,
			final int y1, final int x2, final int y2) {
		final Point p = findFreeSquare(x1, y1, x2, y2);
		if (p == null) {
			return false;
		}
		addThing(thing, p.x, p.y);
		return true;
	}

	/** move thing to location. basically same as addThing */
	public void moveThing(final Thing thing, final int x, final int y) {
		addThing(thing, x, y);
	}

	/** removes thing from map, returns thing if found, null otherwise */
	@Override
	public void removeThing(final Thing thing) {
		if (thing.place != this) {
			throw new Error("Thing in wrong place!");
		}

		final int loc = thing.x + thing.y * width;

		Thing head = objects[loc];

		if (thing.get("LocationModifiers") != null) {
			Thing track = head;
			while (track != null) {
				if (track != thing) {
					thing.unApplyModifiers("LocationModifiers", track);
				}
				track = track.next;
			}
		}

		if (thing.isModified()) {
			thing.removeAllModifiers("LocationModifiers");
		}

		if (head == thing) {
			objects[loc] = thing.next;
			thing.next = null;
			thing.place = null;
			return;
		}

		for (head = objects[loc]; head != null; head = head.next) {
			if (head.next == thing) {
				head.next = thing.next;
				thing.next = null;
				thing.place = null;
				return;
			}
		}
	}

	@Override
	public final BattleMap getMap() {
		return this;
	}

	public boolean isTransparent(final int x, final int y) {
		final int i = x + y * width;
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return false;
		}
		if ((tiles[i] & Tile.TF_TRANSPARENT) == 0) {
			return false;
		}
		Thing track = objects[i];
		while (track != null) {
			if (!track.isTransparent()) {
				return false;
			}
			track = track.next;
		}
		return true;
	}

	/** is location on valid map area */
	public final boolean isValid(final int x, final int y) {
		return x >= 0 && y >= 0 && x < width && y < height;
	}

	/** fast integer LOS algorithm */
	public final boolean isLOS(final int x1, final int y1, final int x2,
			final int y2) {
		if (!(isValid(x1, y1) && isValid(x2, y2))) {
			return false;
		}
		int x = (x1 << 8) + 128;
		int y = (y1 << 8) + 128;
		final int xd = x2 - x1;
		final int yd = y2 - y1;
		int dx;
		int dy;
		int count;
		if (RPG.abs(xd) <= RPG.abs(yd)) {
			if (yd == 0) {
				return true; // check for (0,0)
			}
			dx = (xd << 8) / RPG.abs(yd);
			dy = RPG.sign(yd) << 8;
			count = RPG.abs(yd);
		} else {
			dx = RPG.sign(xd) << 8;
			dy = (yd << 8) / RPG.abs(xd);
			count = RPG.abs(xd);
		}
		while (count > 0) {
			x += dx;
			y += dy;
			if (x >> 8 == x2 && y >> 8 == y2) {
				return true;
			}
			// if ((getTile((x >> 8), (y >> 8)) & Tile.TF_TRANSPARENT) == 0)
			if (!isTransparent(x >> 8, y >> 8)) {
				return false;
			}
			count--;
		}
		return true;
	}

	/** calculates all the visible squares for the hero at given position */
	public void calcVisible(final Thing h) {
		final int combatantid = h.combatant.id;
		final BattleState s = getState();
		VisionCache cache = visioncache.get(combatantid);
		final String perceptiona = h.combatant.perceive(period);
		if (cache != null) {
			if (VisionCache.valid()
					&& cache.equals(new VisionCache(perceptiona, h))) {
				for (Point p : cache.seen) {
					seeTile(p.x, p.y);
				}
				return;
			} else {
				visioncache.clear();
			}
		}
		cache = new VisionCache(perceptiona, h);
		for (Point p : h.combatant.calculatevision(s)) {
			cache.seen.add(p);
			seeTile(p.x, p.y);
		}
		visioncache.put(combatantid, cache);
	}

	public void seeTile(int x, int y) {
		setTileFull(x, y, getTileFull(x, y) | Tile.TF_VISIBLE);
	}

	public void makeAllInvisible() {
		for (int i = 0; i < size; i++) {
			tiles[i] &= ~Tile.TF_VISIBLE;
		}
	}

	public void setAllVisible() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				setVisible(x, y);
			}
		}
	}

	/** Make a square visible */
	public void setVisible(final int x, final int y) {
		final int i = x + y * width;
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return;
		}
		if ((tiles[i] & Tile.TF_VISIBLE) == 0) {
			tiles[i] |= Tile.TF_VISIBLE
			// | Tile.TF_DISCOVERED
			;
			final Thing m = getMobile(x, y);
			if (m != null) {
				if (m.isHostile(Game.hero())) {
					Game.hero().isRunning(false);
				}
			}
		}
	}

	/** an the square currently be seen by the hero */
	public boolean isVisible(final int x, final int y) {
		final Thing h = Game.hero();
		final boolean yes = h.x == x && h.y == y || isHeroLOS(x, y);
		// System.out.println("[BattleMap] visible? " + yes);
		return yes;
	}

	/** is the square currently in LOS for the hero */
	public boolean isHeroLOS(final int x, final int y) {
		return (getTileFlags(x, y) & Tile.TF_VISIBLE) > 0;
	}

	/**
	 * Can the square currently be seen by the hero assumes bound checking
	 * already done
	 */
	public boolean isVisibleChecked(final int i) {
		return (tiles[i] & Tile.TF_VISIBLE) > 0;
		// return isVisible(x, y);
	}

	public boolean isEmpty(final int x, final int y) {
		return getObjects(x, y) == null;
	}

	public boolean isClear(final int x, final int y) {
		return isEmpty(x, y) && !isBlocked(x, y);
	}

	public Point tracePath(final int x1, final int y1, final int x2,
			final int y2) {
		final int xd = x2 - x1;
		final int yd = y2 - y1;
		if (xd == 0 && yd == 0) {
			return new Point(x1, y1);
		}
		final double d = Math.sqrt(xd * xd + yd * yd);
		final double dx = xd / d;
		final double dy = yd / d;
		double x = x1 + 0.5 + dx;
		double y = y1 + 0.5 + dy;
		for (int i = 0; i < 100 && (int) x != x2 || (int) y != y2; i++) {
			x = x + dx;
			y = y + dy;
			final int cx = (int) x;
			final int cy = (int) y;
			if ((getTileFull(cx, cy) & Tile.TF_BLOCKED) != 0) {
				return new Point((int) (x - dx), (int) (y - dy));
			}
			if (isBlocked(cx, cy)) {
				return new Point(cx, cy);
			}
		}
		return new Point(x2, y2);
	}

	/**
	 * Returns true if the square contain a solid wall, monster or hard scenery
	 */
	public boolean isBlocked(final int x, final int y) {
		if (isTileBlocked(x, y)) {
			return true;
		}
		Thing tracker = objects[x + y * width];
		while (tracker != null) {
			if (tracker.isBlocking()) {
				return true;
			}
			tracker = tracker.next;
		}
		return false;
	}

	/**
	 * Returns true if a tile is blocked by a solid wall
	 * 
	 * @author Mike
	 */
	public final boolean isTileBlocked(final int x, final int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return true;
		}
		return (tiles[x + y * width] & Tile.TF_BLOCKED) != 0;
	}

	/**
	 * Returns true if a tile is blocked by a blocking object
	 * 
	 * @author Mike
	 */
	public boolean isObjectBlocked(final int x, final int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return true;
		}
		final int i = x + y * width;
		Thing tracker = objects[i];
		while (tracker != null) {
			if (tracker.isBlocking()) {
				return true;
			}
			tracker = tracker.next;
		}
		return false;
	}

	public final boolean isTileAdjacent(final int x, final int y,
			final int tile) {
		if (getTile(x - 1, y) == tile) {
			return true;
		}
		if (getTile(x + 1, y) == tile) {
			return true;
		}
		if (getTile(x, y - 1) == tile) {
			return true;
		}
		if (getTile(x, y + 1) == tile) {
			return true;
		}
		return false;
	}

	/** get path code */
	public int getPath(final int x, final int y) {
		if (path == null) {
			path = new int[size];
		}
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return -1;
		}
		return path[x + y * width];
	}

	/** map metrics */
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Game getGame() {
		return game;
	}

	/** get tile type */
	public final int getTile(final int x, final int y) {
		final int i = x + width * y;
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return 0; // off-map tiles blank
		}
		return tiles[i] & Tile.TF_TYPEMASK;
	}

	/** get tile including flags */
	protected final int getTileFull(final int x, final int y) {
		final int i = x + width * y;
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return Tile.TF_BLOCKED; // off-map tiles blocked
		}
		return tiles[i];
	}

	/** get Tile.TF_ flags for specified tile */
	public final int getTileFlags(final int x, final int y) {
		final int i = x + width * y;
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return Tile.TF_BLOCKED; // off-map tiles blocked
		}
		return tiles[i] & ~Tile.TF_TYPEMASK;
	}

	/** get linked list of objects in square */
	public Thing getObjects(final int x, final int y) {
		final int i = x + width * y;
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return null;
		}
		return objects[i];
	}

	/** get linked list of objects in square */
	public Thing getObjectsChecked(final int i) {
		return objects[i];
	}

	/**
	 * Get object with specified flag set in a specified square Returns first
	 * object found if more than one
	 */
	public Thing getFlaggedObject(final int x, final int y, final String flag) {
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return null;
		}
		final int i = x + width * y;
		return getFlaggedObject(i, flag);
	}

	public Thing getFlaggedObject(final int i, final String flag) {
		Thing t = objects[i];
		while (t != null) {
			if (t.getFlag(flag)) {
				return t;
			}
			t = t.next;
		}
		return null;
	}

	/** get object with specified name */
	public Thing getNamedObject(final int x, final int y, final String name) {
		final int i = x + width * y;
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return null;
		}
		Thing t = objects[i];
		while (t != null) {
			if (name.equals(t.get("Name"))) {
				return t;
			}
			t = t.next;
		}
		return null;
	}

	/**
	 * Add something to the map, keeping all references updated.
	 * 
	 * Can assume that the Thing is already remove()'d
	 */
	private final Thing addObject(final Thing thing, final int x, final int y) {
		if (thing == null) {
			return null;
		}
		if (x >= width || x < 0) {
			return null;
		}
		if (y >= height || y < 0) {
			return null;
		}

		final Thing head = objects[x + y * width];

		final boolean item = thing.getFlag("IsItem");

		{
			Thing track = head;
			while (track != null) {
				if (item && thing.stackWith(track)) {
					return track;
				}
				if (track.get("LocationModifiers") != null) {
					track.applyModifiers("LocationModifiers", thing);
				}
				track = track.next;
			}
		}

		if (thing.get("LocationModifiers") != null) {
			Thing track = head;
			while (track != null) {
				thing.applyModifiers("LocationModifiers", track);
				track = track.next;
			}
		}

		thing.next = objects[x + y * width];
		objects[x + y * width] = thing;
		thing.place = this;
		thing.x = x;
		thing.y = y;
		return thing;
	}

	/** set tile using default flags */
	public final void setTile(final int x, final int y, final int t) {
		final int i = x + y * width;
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return;
		}
		tiles[i] = t | Tile.getMask(t);
	}

	/** set tile, including tile flags */
	public final void setTileFull(final int x, final int y, final int t) {
		final int i = x + y * width;
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return;
		}
		tiles[i] = t;
	}

	/**
	 * gets a mobile from given square returns Mobile or null if none available
	 */
	public Thing getMobile(final int x, final int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return null;
		}
		final int i = x + y * width;
		Thing tracker = objects[i];
		while (tracker != null) {
			if (tracker.isMobile()) {
				return tracker;
			}
			tracker = tracker.next;
		}
		return null;
	}

	/**
	 * gets a mobile from given square returns Mobile or null if none available
	 */
	public Thing getMobileChecked(final int i) {
		Thing tracker = objects[i];

		while (tracker != null) {
			if (tracker.isMobile()) {
				return tracker;
			}
			tracker = tracker.next;
		}
		return null;
	}

	/** functions to detect/return obvious nearby mobiles */
	public int countNearby(final String flag, final int x, final int y,
			final int r) {
		int count = 0;
		for (int lx = x - r; lx <= x + r; lx++) {
			for (int ly = y - r; ly <= y + r; ly++) {
				if (getFlaggedObject(lx, ly, flag) != null) {
					count++;
				}
			}
		}
		return count;
	}

	public Thing getNearby(final String flag, final int x, final int y,
			final int r) {
		for (int lx = x - r; lx <= x + r; lx++) {
			for (int ly = y - r; ly <= y + r; ly++) {
				if (lx == x && ly == y) {
					continue;
				}
				final Thing m = getFlaggedObject(lx, ly, flag);
				if (m != null) {
					return m;
				}
			}
		}
		return getFlaggedObject(x, y, flag);
	}

	/** do damage in circular area (size r2 = radius squared) */
	public void areaDamage(final int x, final int y, final int r2,
			final int dam, final String damtype) {
		int d = 0;
		for (d = 0; d * d <= r2; d++) {
			// spin
		}
		d = d - 1;
		final Thing[] things = getThings(x - d, y - d, x + d, y + d);
		for (final Thing t : things) {
			if ((t.x - x) * (t.x - x) + (t.y - y) * (t.y - y) <= r2) {
				Damage.inflict(t, RPG.a(dam), damtype);
			}
		}
	}

	/** do damage in circular area (size r2 = radius squared) */
	public void areaDamage(final int x, final int y, final int dam,
			final String damtype) {
		final Thing[] things = getThings(x, y);
		for (final Thing t : things) {
			if (t.getFlag("IsPhysical")) {
				Damage.inflict(t, RPG.a(dam), damtype);
			}
		}
	}

	/**
	 * Sorts objects in square into correct Z order returns head item (lowest Z
	 * value)
	 */
	public Thing sortZ(final int x, final int y) {
		final Thing head = objects[x + y * width];
		if (head == null) {
			return null;
		}
		return objects[x + y * width] = sortZGetFirst(head);
	}

	/** utility function for sortZ */
	private Thing sortZGetFirst(final Thing top) {
		if (top.next == null) {
			return top;
		}
		top.next = sortZGetFirst(top.next);

		if (top.getZ() <= top.next.getZ()) {
			return top;
		}
		final Thing t = top.next;
		top.next = t.next;
		t.next = sortZGetFirst(top);
		return t;
	}

	// Functions to return array of things in area

	/** return all things on map */
	public Thing[] getThings() {
		return getThings(0, 0, width - 1, height - 1);
	}

	/** return all things in square */
	public Thing[] getThings(final int x, final int y) {
		return getThings(x, y, x, y);
	}

	/** return all objects with given flag in rectangular area */
	public Thing[] getObjects(int x1, int y1, int x2, int y2,
			final String flag) {
		if (x1 < 0) {
			x1 = 0;
		}
		if (x2 >= width) {
			x2 = width - 1;
		}
		if (y1 < 0) {
			y1 = 0;
		}
		if (y2 >= height) {
			y2 = height - 1;
		}
		int count = 0;
		for (int y = y1; y <= y2; y++) {
			for (int x = x1; x <= x2; x++) {
				Thing tracker = objects[x + y * width];
				while (tracker != null) {
					if (tracker.getFlag(flag)) {
						count++;
					}
					tracker = tracker.next;
				}
			}
		}

		if (count == 0) {
			return BattleMap.emptyThings;
		}
		final Thing[] things = new Thing[count];

		count = 0;
		for (int y = y1; y <= y2; y++) {
			for (int x = x1; x <= x2; x++) {
				Thing tracker = objects[x + y * width];
				while (tracker != null) {
					if (tracker.getFlag(flag)) {
						things[count] = tracker;
						count++;
					}
					tracker = tracker.next;
				}
			}
		}
		return things;
	}

	private static final Thing[] emptyThings = new Thing[] {};

	/** return all things in rectangular area */
	public Thing[] getThings(int x1, int y1, int x2, int y2) {
		if (x1 < 0) {
			x1 = 0;
		}
		if (x2 >= width) {
			x2 = width - 1;
		}
		if (y1 < 0) {
			y1 = 0;
		}
		if (y2 >= height) {
			y2 = height - 1;
		}
		int count = 0;
		for (int y = y1; y <= y2; y++) {
			for (int x = x1; x <= x2; x++) {
				Thing tracker = objects[x + y * width];
				while (tracker != null) {
					count++;
					tracker = tracker.next;
				}
			}
		}

		if (count == 0) {
			return BattleMap.emptyThings;
		}
		final Thing[] things = new Thing[count];

		count = 0;
		for (int y = y1; y <= y2; y++) {
			for (int x = x1; x <= x2; x++) {
				Thing tracker = objects[x + y * width];
				while (tracker != null) {
					things[count] = tracker;
					count++;
					tracker = tracker.next;
				}
			}
		}
		return things;
	}

	/**
	 * execute map action for <time> ticks this calls all monster, trap and item
	 * actions
	 */
	public void action(final Event ae) {
		// add delayed additions objects
		Game.instance().addMapObjects(this);

		final Thing[] things = getThings();
		final int time = ae.getStat("Time");
		final Script s = (Script) get("OnAction");

		if (s != null) {
			if (s.handle(null, ae)) {
				return;
			}
		}

		// now do stuff
		for (final Thing t : things) {
			// if (t==Game.hero) continue;
			// note: place could theoretically change (e.g. dead)
			if (t.place == this) {
				t.action(ae);
			}
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if ((tiles[x + y * width] & Tile.TF_ACTIVE) > 0) {
					Tile.action(this, x, y, time);
				}
			}
		}
	}

	/** get portal for specific square */
	public Thing[] getPortals(final int tx, final int ty) {
		return getObjects(tx, ty, tx, ty, "IsPortal");
	}

	public ArrayList getAllPortals() {
		final ArrayList al = new ArrayList();
		final Thing[] ps = getObjects(0, 0, width - 1, height - 1, "IsPortal");
		for (final Thing element : ps) {
			al.add(element);
		}
		final Thing entrance = getEntrance();
		if (entrance != null && !al.contains(entrance)) {
			al.add(entrance);
		}
		final Thing exit = getExit();
		if (exit != null && !al.contains(exit)) {
			al.add(exit);
		}
		return al;
	}

	/** exit map from specified location */
	public void exitMap(final int tx, final int ty) {
		if (getFlag("IsWorldMap")) {
			WorldMap.exitWorldMap(this, tx, ty);
		} else {
			final Thing h = Game.hero();
			Thing p = null;

			final Thing[] ps = getPortals(tx, ty);
			if (ps.length == 1) {
				p = ps[0];
			} else if (ps.length > 1) {
				p = Game.selectItem("Select a route:", ps);
			}

			// use default portal
			if (p == null && canExit()) {
				p = getEntrance();
			}

			if (p != null) {
				// The negative energy of aggresivity makes these portal
				// crumbles
				// I will not have the portal stone to be the perfect escape
				// plan ;)
				if (p.toString().startsWith("traveler portal")) {
					// We are counting on the hero to stand on the portal by
					// now...
					// Otherwise we are in a world of pain ...
					final Thing being = findNearestFoe(Game.hero());
					System.out.println("Foe found " + being);
					if (being != null) {
						int distx = being.x - p.x;
						int disty = being.x - p.y;
						distx = distx < 0 ? distx * -1 : distx; // Cheap man's
						// abs
						disty = disty < 0 ? disty * -1 : disty;
						if (distx < 5 || disty < 5) {
							Game.messageTyrant(
									"The portal crumbles in the presence of the angry "
											+ being);
							p.die();
							return;
						}
					}
				}
				Portal.travel(p, h);
			}
		}
	}

	/** set the message for this particular map */
	public String getEnterMessage() {
		String s = getString("EnterMessageFirst");
		if (s != null) {
			set("EnterMessageFirst", null);
		} else {
			s = getString("EnterMessage");
		}
		if (s == null) {
			return "";
		}
		return s;
	}

	/** Level name for display on status panel */
	public String getDescription() {
		String s = getString("Description");
		if (s == null) {
			s = "Unknown area";
			Game.warn("No map Description!");
		}
		final int dlevel = getStat("DungeonLevel");
		if (dlevel > 0) {
			s = s + " Lv. " + dlevel;
		}
		return s;
	}

	/**
	 * @param entrance
	 *            The entrance to set.
	 */
	public void setEntrance(final Thing entrance) {
		setPortal("entrance", entrance);
	}

	public Thing addEntrance(final String name) {
		return addEntrance(Portal.create(name));
	}

	public Thing addEntrance(final Thing p) {
		addThing(p);
		setEntrance(p);
		return p;
	}

	/**
	 * @return Returns the entrance.
	 */
	public Thing getEntrance() {
		return getPortal("entrance");
	}

	public void setPortal(final String portalName, final Thing portal) {
		set("Portal:" + portalName, portal);
		if (portal != null) {
			portal.set("PortalName", portalName);
		}
	}

	/**
	 * @return Returns a named portal.
	 */
	public Thing getPortal(final String portalName) {
		return getThing("Portal:" + portalName);
	}

	/**
	 * @param exit
	 *            The exit to set.
	 */
	public void setExit(final Thing exit) {
		setPortal("exit", exit);
	}

	/**
	 * @return Returns the exit.
	 */
	public Thing getExit() {
		return getPortal("exit");
	}

	public String getTileXML() {
		final StringBuffer s = new StringBuffer();
		// s.append("<tiles>\n");
		// for (int y = 0; y < height; y++) {
		// s.append("<row>");
		// for (int x = 0; x < width; x++) {
		// final String ns = Text
		// .leftPad(Integer.toString(getTile(x, y) & 65535), 4);
		// s.append(ns + " ");
		// }
		// s.append("</row>\n");
		// }
		// s.append("</tiles>\n");
		return s.toString();
	}

	public String getLevelXML() {
		final StringBuffer s = new StringBuffer();

		s.append("<map width=\"" + width + "\" height=\"" + height + "\">");
		s.append(getTileXML());
		s.append("</map>");
		return s.toString();
	}

	@Override
	public String toString() {
		return new MapHelper().mapToString(this);
	}

	public String name() {
		return getString("HashName");
	}

	public void visitPath(final int x, final int y) {
		if (path == null) {
			path = new int[size];
		}
		path[x + y * width] = 1;
	}

	public int[] getTiles() {
		return tiles;
	}

	public void setTiles(final int[] tiles) {
		this.tiles = tiles;
	}

	public Thing[] getObjects() {
		return objects;
	}

	public void setObjects(final Thing[] objects) {
		this.objects = objects;
	}

	public BattleState getState() {
		final Square[][] map = new Square[width][height];
		for (final Combatant c : BattleMap.combatants) {
			final int[] location = c.determineLocation();
			if (isBlocked(location[0], location[1])) {
				removeThing(c.visual);
				newSquare(map, location[0], location[1]);
				addThing(c.visual, location[0], location[1]);
			}
		}
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (map[x][y] == null) {
					newSquare(map, x, y);
				}
			}
		}
		final ArrayList<Combatant> blueTeam = new ArrayList<Combatant>();
		final ArrayList<Combatant> redTeam = new ArrayList<Combatant>();
		for (final Combatant c : BattleMap.combatants) {
			(BattleMap.blueTeam.contains(c) ? blueTeam : redTeam).add(c);
		}
		return new BattleState(blueTeam, redTeam,
				(ArrayList<Combatant>) BattleMap.dead.clone(), map, period,
				meld);
	}

	private void newSquare(final Square[][] map, final int x, final int y) {
		map[x][y] = BattleScreen.active.getsquare(x, y);
	}

	public void setState(final Node node) {
		final TreeMap<Integer, Thing> temp = new TreeMap<Integer, Thing>();
		for (final Combatant c : BattleMap.combatants) {
			c.visual.remove();
			temp.put(c.id, c.visual);
		}
		final BattleState state = (BattleState) node;
		BattleMap.combatants = state.getCombatants();
		BattleMap.blueTeam = new ArrayList<Combatant>(state.getBlueTeam());
		BattleMap.redTeam = new ArrayList<Combatant>(state.getRedTeam());
		for (final Combatant c : BattleMap.combatants) {
			Thing t = temp.get(c.id);
			if (t == null) {
				t = BattleSetup.addThing(this,
						new Point(c.location[0], c.location[1]), "dog");
			}
			addThing(t, c.location[0], c.location[1]);
			c.visual = t;
			t.combatant = c;
		}
		BattleMap.dead = state.dead;
		period = state.period;
		meld = state.meld;
	}

	public void flood(BattleState state) {
		for (int x = 0; x < state.map.length; x++) {
			Square[] squares = state.map[x];
			for (int y = 0; y < squares.length; y++) {
				Square s = squares[y];
				if (s.flooded) {
					Map.addthing(x, y, Javelin.app.fight.map.flooded, this);
				}
			}
		}
	}

	static public Meld checkformeld(int x, int y) {
		Meld meld = null;
		for (Meld m : BattleScreen.active.map.meld) {
			if (m.x == x && m.y == y) {
				meld = m;
				break;
			}
		}
		return meld;
	}
}