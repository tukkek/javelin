package javelin.model.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.TreeMap;

import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Tile;

/**
 * Overworld view of the game world.
 * 
 * 2/16 Easy (el-5 to el-8) - plains
 * 
 * 10/16 Moderate (el-4) - forest
 * 
 * 3/16 Difficult (el-3 to el) - mountains
 * 
 * 1/16 Very difficult (el+1) - swamp
 * 
 * forest is the background. have 2 plain areas (1 is player start), 3 mountain
 * areas and 1 swamp.
 * 
 * TODO would be nice to have tiles reflect the official d20 terrains (add
 * desert and hill)
 * 
 * @author alex
 */
public class WorldMap implements Serializable {
	public enum Region {
		EASYA, EASYB, NORMALA, HARDA, HARDB, HARDC, VERYHARDA
	}

	public static final int EASY = Tile.PLAINS;
	public static final int MEDIUM = Tile.FORESTS;
	public static final int HARD = Tile.HILLS;
	public static final int VERYHARD = Tile.GUNK;

	public static final int TOWNBUFFER = 1;
	private static final int[] NOISE = new int[] { 0, 2, 3 };
	static public int MAPDIMENSION = 30;
	private static final int NOISEAMOUNT = MAPDIMENSION * MAPDIMENSION / 10;

	final Region[][] map = new Region[MAPDIMENSION][MAPDIMENSION];
	public TreeMap<Region, Point> towns;
	public static WorldMap seed = new WorldMap();

	public WorldMap() {
		for (int i = 0; i < MAPDIMENSION; i++) {
			for (int j = 0; j < MAPDIMENSION; j++) {
				map[i][j] = Region.NORMALA;
			}
		}

		while (towns == null || !validatetowns()) {
			towns = placetowns();
		}
		for (final Entry<Region, Point> town : towns.entrySet()) {
			final Point p = town.getValue();
			for (int x = p.x - 1; x <= p.x + 1; x++) {
				for (int y = p.y - 1; y <= p.y + 1; y++) {
					map[x][y] = town.getKey();
				}
			}
		}
		final TreeMap<Region, Integer> regionsizes =
				new TreeMap<Region, Integer>();
		for (final Region r : Region.values()) {
			if (r == Region.NORMALA) {
				continue;
			}
			regionsizes.put(r, 9);
		}
		while (isbuilding(regionsizes)) {
			buildregions(regionsizes);
		}
		addnoise();
	}

	public void buildregions(final TreeMap<Region, Integer> regionsizes) {
		Region expand = null;
		for (final Region r : Region.values()) {
			if (r == Region.NORMALA) {
				continue;
			}
			if (expand == null
					|| regionsizes.get(r) < regionsizes.get(expand)) {
				expand = r;
			}
		}
		final Region square = expand(expand, towns.get(expand), 0);
		updatesize(expand, 1, regionsizes);
		if (square != Region.NORMALA) {
			updatesize(square, -1, regionsizes);
		}
	}

	public Region expand(Region r, Point p, int maxtries) {
		int x = p.x;
		int y = p.y;
		Integer lastx;
		Integer lasty;
		int tries = 0;
		while (map[x][y] == r) {
			tries += 1;
			if (maxtries != 0 && tries >= maxtries) {
				return null;
			}
			lastx = x;
			lasty = y;
			x += randomstep();
			y += randomstep();
			if (outside(x) || outside(y)) {
				x = lastx;
				y = lasty;
				continue;
			}
			/* collision with other town */
			for (final Point neighbor : towns.values()) {
				if (p == neighbor) {
					continue;
				}
				for (int nx = neighbor.x - TOWNBUFFER; nx <= neighbor.x
						+ TOWNBUFFER; nx++) {
					for (int ny = neighbor.y - TOWNBUFFER; ny <= neighbor.y
							+ TOWNBUFFER; ny++) {
						if (x == nx && y == ny) {
							x = lastx;
							y = lasty;
							continue;
						}
					}
				}
			}
		}
		final Region square = map[x][y];
		map[x][y] = r;
		return square;
	}

	private void addnoise() {
		int noiseleft = NOISEAMOUNT;
		while (noiseleft > 0) {
			int chunk = RPG.r(4, 10);
			if (chunk < 1) {
				chunk = 1;
			} else if (chunk > noiseleft) {
				chunk = noiseleft;
			}
			int x = RPG.r(0, MAPDIMENSION - 1), y = RPG.r(0, MAPDIMENSION - 1);
			Region r = null;
			while (r == null || r == map[x][y]) {

				r = Region.values()[RPG.pick(NOISE)];
			}
			for (int i = 0; i < chunk; i++) {
				if (expand(r, new Point(x, y), 1000) == null) {
					break;
				}
				noiseleft -= 1;
			}
		}
	}

	public Integer updatesize(final Region expand, final int i,
			final TreeMap<Region, Integer> regionsizes) {
		return regionsizes.put(expand, regionsizes.get(expand) + i);
	}

	private boolean outside(final int y) {
		return y < 0 || y >= MAPDIMENSION;
	}

	private int randomstep() {
		return RPG.pick(new int[] { -1, 0, +1 });
	}

	private boolean isbuilding(final TreeMap<Region, Integer> regionsizes) {
		for (final Entry<Region, Integer> size : regionsizes.entrySet()) {
			if (size.getKey() == Region.NORMALA) {
				continue;
			}
			if (size.getValue() < MAPDIMENSION * MAPDIMENSION / 16) {
				return true;
			}
		}
		return false;
	}

	private boolean validatetowns() {
		for (final Point a : towns.values()) {
			for (final Point b : towns.values()) {
				if (a == b) {
					continue;
				}
				if (triangledistance(a, b) < TOWNBUFFER) {
					return false;
				}
			}
		}
		return true;
	}

	static public double triangledistance(final Point a, final Point b) {
		return Math.sqrt(calcdist(a.x - b.x) + calcdist(a.y - b.y));
	}

	static private int calcdist(final int deltax) {
		final int abs = Math.abs(deltax);
		return abs * abs;
	}

	public TreeMap<Region, Point> placetowns() {
		final TreeMap<Region, Point> towns = new TreeMap<Region, Point>();
		final ArrayList<Region> regions = new ArrayList<Region>();
		for (final Region r : Region.values()) {
			regions.add(r);
		}
		Collections.shuffle(regions);
		for (final Region r : regions) {
			placetown(towns, r);
		}
		return towns;
	}

	protected void placetown(final TreeMap<Region, Point> towns,
			final Region r) {
		Point proposal = null;
		placement: while (proposal == null) {
			proposal = new Point(randomaxispoint(), randomaxispoint());
			for (final Point town : towns.values()) {
				for (int x = town.x - TOWNBUFFER; x <= town.x
						+ TOWNBUFFER; x++) {
					for (int y = town.y - TOWNBUFFER; y <= town.y
							+ TOWNBUFFER; y++) {
						if (proposal.x == x && proposal.y == y) {
							proposal = null;
							continue placement;
						}
					}
				}
			}
		}
		towns.put(r, proposal);
	}

	private int randomaxispoint() {
		return RPG.r(1, MAPDIMENSION - 2);
	}

	public static void main(final String[] args) {
		for (int i = 0; i < 50; i++) {
			new WorldMap();
		}
		System.out.println("done");
		final WorldMap test = new WorldMap();
		final Collection<Point> towns = test.towns.values();
		for (int i = 0; i < MAPDIMENSION; i++) {
			String line = "";
			for (int j = 0; j < MAPDIMENSION; j++) {
				Character letter = null;
				for (final Point town : towns) {
					if (town.x == i && town.y == j) {
						letter = 'T';
					}
				}
				if (letter == null) {
					switch (test.map[i][j]) {
					case EASYA:
					case EASYB:
						letter = 'e';
						break;
					default:
					case NORMALA:
						letter = 'n';
						break;
					case HARDA:
					case HARDB:
					case HARDC:
						letter = 'h';
						break;
					case VERYHARDA:
						letter = 'v';
						break;
					}
				}
				line += letter;
			}
			System.out.println(line);
		}
	}

	public int getTile(final int i, final int j) {
		switch (map[i][j]) {
		case EASYA:
		case EASYB:
			return Tile.PLAINS;
		default:
		case NORMALA:
			return Tile.FORESTS;
		case HARDA:
		case HARDB:
		case HARDC:
			return Tile.HILLS;
		case VERYHARDA:
			return Tile.GUNK;
		}
	}

	static public boolean isoccupied(final int x, final int y,
			boolean townbufferenabled) {
		if (WorldScreen.getactor(x, y) != null) {
			return true;
		}
		if (townbufferenabled) {
			for (final Point p : seed.towns.values()) {
				for (int townx = p.x - TOWNBUFFER; townx <= p.x
						+ TOWNBUFFER; townx++) {
					for (int towny = p.y - TOWNBUFFER; towny <= p.y
							+ TOWNBUFFER; towny++) {
						if (townx == x && towny == y) {
							return true;
						}
					}
				}
			}
		} else {
			for (final Point p : seed.towns.values()) {
				if (p.x == x && p.y == y) {
					return true;
				}
			}
		}
		return false;
	}
}
