package javelin.model.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import javelin.controller.Point;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.terrain.Terrain;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.town.Town;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * Game world overview. This is focused on generating the initial game state.
 * 
 * TODO would be nice to have tiles reflect the official d20 terrains (add
 * desert and hill)
 * 
 * @see WorldScreen
 * @author alex
 */
public class World implements Serializable {
	/**
	 * Map size in squares.
	 */
	static public int MAPDIMENSION = 30;
	/**
	 * Randomly generated world map.
	 */
	public static World seed;
	/** Facilitate movement. */
	public static boolean[][] roads = new boolean[MAPDIMENSION][MAPDIMENSION];
	/** Upgraded {@link #roads}. */
	public static boolean[][] highways =
			new boolean[MAPDIMENSION][MAPDIMENSION];
	private static int retries = 0;
	private static int lastretries = 0;
	static final int TOWNBUFFER = 1;
	static final Terrain[] NOISE = new Terrain[] { Terrain.PLAIN, Terrain.HILL,
			Terrain.FOREST, Terrain.MOUNTAINS };
	static final int NOISEAMOUNT = MAPDIMENSION * MAPDIMENSION / 10;

	/** Map of terrain tiles by [x][y] coordinates. */
	public Terrain[][] map = new Terrain[MAPDIMENSION][MAPDIMENSION];
	/** If <code>false</code> means the world is still being generated. */
	public boolean done = false;

	/**
	 * Arbitrary number to serve as guideline for {@link Terrain} generation.
	 */
	public static final int NREGIONS = 16;

	void generate() {
		for (int i = 0; i < MAPDIMENSION; i++) {
			for (int j = 0; j < MAPDIMENSION; j++) {
				map[i][j] = Terrain.FOREST;
			}
		}
		initroads();
		LinkedList<Realm> realms = new LinkedList<Realm>();
		for (Realm r : Realm.values()) {
			realms.add(r);
		}
		Collections.shuffle(realms);
		Terrain.MOUNTAINS.generate(this, realms.pop());
		Terrain.MOUNTAINS.generate(this, realms.pop());
		Terrain.DESERT.generate(this, realms.pop());
		Terrain.PLAIN.generate(this, realms.pop());
		Terrain.HILL.generate(this, realms.pop());
		Terrain.WATER.generate(this, null);
		Terrain.WATER.generate(this, null);
		Terrain.MARSH.generate(this, realms.pop());
		Terrain.FOREST.generate(this, realms.pop());
	}

	void initroads() {
		for (int x = 0; x < MAPDIMENSION; x++) {
			for (int y = 0; y < MAPDIMENSION; y++) {
				roads[x][y] = false;
				highways[x][y] = false;
			}
		}
	}

	/**
	 * @return <code>true</code> if given coordinates are within the world map.
	 */
	public static boolean validatecoordinate(int x, int y) {
		return 0 <= x && x < MAPDIMENSION && 0 <= y && y < MAPDIMENSION;
	}

	/**
	 * @param p
	 *            Given a location...
	 * @return the {@link Realm} of the closest {@link Town}.
	 */
	public static WorldActor determinecolor(Point p) {
		ArrayList<WorldActor> towns = Location.getall(Town.class);
		WorldActor closest = towns.get(0);
		for (int i = 1; i < towns.size(); i++) {
			Town t = (Town) towns.get(i);
			if (t.realm != null && Walker.distance(t.x, t.y, p.x, p.y) < Walker
					.distance(closest.x, closest.y, p.x, p.y)) {
				closest = t;
			}
		}
		return closest;
	}

	/**
	 * Create a map at the start of the game.
	 * 
	 * @return Starting point (Town)
	 */
	public static void makemap() {
		Town start = null;
		while (seed == null) {
			try {
				seed = new World();
				seed.generate();
				placemoretowns();
				start = FeatureGenerator.SINGLETON.placestartingfeatures();
			} catch (RestartWorldGeneration e) {
				seed = null;
				Town.initnames();
				ArrayList<WorldActor> squad =
						WorldActor.INSTANCES.get(Squad.class);
				WorldActor.INSTANCES.clear();
				WorldActor.INSTANCES.put(Squad.class, squad);
			}
		}
		finish(start);
	}

	static void finish(Town start) {
		start.garrison.clear();
		start.capture(true);
		placenearbywoods(start);
		Squad.active.x = start.x;
		Squad.active.y = start.y;
		Squad.active.displace();
		Squad.active.place();
		Squad.active.equipment.fill(Squad.active);
		Squad.active.lasttown = start;
		Outpost.discover(start.x, start.y, Outpost.VISIONRANGE);
		seed.done = true;
	}

	static void placemoretowns() {
		int more = RPG.r(5, 7);
		while (more > 0) {
			int x = RPG.r(0, MAPDIMENSION - 1);
			int y = RPG.r(0, MAPDIMENSION - 1);
			if (WorldActor.get(x, y) != null
					|| !seed.map[x][y].generatetown(new Point(x, y), seed)) {
				continue;
			}
			more -= 1;
			Point p = new Point(x, y);
			Town t = new Town(p.x, p.y, World.determinecolor(p).realm);
			while (t.iscloseto(Town.class)) {
				Town.generate(t);
			}
			t.place();
		}
	}

	static void placenearbywoods(Town start) {
		int x, y;
		int minx = start.x - Outpost.VISIONRANGE;
		int maxx = start.x + Outpost.VISIONRANGE;
		int miny = start.y - Outpost.VISIONRANGE;
		int maxy = start.y + Outpost.VISIONRANGE;
		for (x = minx; x <= maxx; x++) {
			for (y = miny; y <= maxy; y++) {
				if (validatecoordinate(x, y)
						&& seed.map[x][y].equals(Terrain.FOREST)) {
					return; // already has nearby woods
				}
			}
		}
		x = -1;
		y = -1;
		while (!validatecoordinate(x, y) || !validatecoordinate(x + 1, y)
				|| WorldActor.get(x, y) != null
				|| WorldActor.get(x + 1, y) != null) {
			x = RPG.r(minx, maxx);
			y = RPG.r(miny, maxy);
		}
		seed.map[x][y] = Terrain.FOREST;
		seed.map[x + 1][y] = Terrain.FOREST;
	}

	/**
	 * @param townbufferenabled
	 *            If <code>true</code> will also return <code>true</code> if too
	 *            close to a {@link Town}.
	 * @return <code>true</code> if there is a {@link Town} in this coordinate
	 *         already.
	 */
	static public boolean istown(final int x, final int y,
			boolean townbufferenabled) {
		if (WorldActor.get(x, y) != null) {
			return true;
		}
		ArrayList<WorldActor> towns = WorldActor.getall(Town.class);
		if (townbufferenabled) {
			for (final WorldActor p : towns) {
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
			for (final WorldActor p : towns) {
				if (p.x == x && p.y == y) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Handles when {@link World} generation is taking too long.
	 * 
	 * @throws RestartWorldGeneration
	 */
	public static void retry() {
		if (World.seed != null && World.seed.done) {
			return;
		}
		retries += 1;
		if (retries > 100000 * 10) {
			retries = 0;
			lastretries = 0;
			throw new RestartWorldGeneration();
		}
		if (retries - lastretries >= 100000 * 2) {
			new InfoScreen("")
					.print("Generating world... Terrain retries: " + retries);
			lastretries = retries;
		}
	}

	@Override
	public String toString() {
		String s = "";
		for (int y = 0; y < MAPDIMENSION; y++) {
			for (int x = 0; x < MAPDIMENSION; x++) {
				s += map[x][y].representation;
			}
			s += "\n";
		}
		return s;
	}
}
