package javelin.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.db.Preferences;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.terrain.Terrain;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.town.Town;
import javelin.view.screen.InfoScreen;
import tyrant.mikera.engine.RPG;

public class WorldGenerator extends Thread {
	private static final boolean SINGLETHREAD = true;
	private static final int MAXRETRIES = 1000;
	public static final Terrain[] GENERATIONORDER = new Terrain[] {
			Terrain.MOUNTAINS, Terrain.MOUNTAINS, Terrain.DESERT, Terrain.PLAIN,
			Terrain.HILL, Terrain.WATER, Terrain.WATER, Terrain.MARSH,
			Terrain.FOREST };
	/**
	 * Arbitrary number to serve as guideline for {@link Terrain} generation.
	 */
	public static final int NREGIONS = 16;
	static final int NOISEAMOUNT = World.scenario.size * World.scenario.size
			/ 10;
	static final Terrain[] NOISE = new Terrain[] { Terrain.PLAIN, Terrain.HILL,
			Terrain.FOREST, Terrain.MOUNTAINS };
	public static final int TOWNBUFFER = 1;
	private static int discarded = 0;

	public World world;
	public int retries = 0;

	/**
	 * @param p
	 *            Given a location...
	 * @return the {@link Realm} of the closest {@link Town}.
	 */
	public static Actor determinecolor(Point p) {
		ArrayList<Actor> towns = World.getall(Town.class);
		Actor closest = towns.get(0);
		for (int i = 1; i < towns.size(); i++) {
			Town t = (Town) towns.get(i);
			if (t.realm != null && Walker.distance(t.x, t.y, p.x, p.y) < Walker
					.distance(closest.x, closest.y, p.x, p.y)) {
				closest = t;
			}
		}
		return closest;
	}

	@Override
	public void run() {
		try {
			world = new World();
			generate(world);
			Town start = FeatureGenerator.SINGLETON
					.placestartingfeatures(world);
			// boolean found = false;
			for (Actor a : world.actors.get(Town.class)) {
				if (a != start) {
					((Town) a).populategarisson();
					// } else {
					// found = true;
				}
			}
			// if (!found) {
			// System.out.println("wut");
			// }
			WorldGenerator.finish(start, world);
		} catch (RestartWorldGeneration e) {
			if (World.seed == null) {
				new WorldGenerator().start();
			}
		}
	}

	public synchronized static void finish(Town start, World w) {
		if (World.seed != null) {
			return;
		}
		World.seed = w;
		start.captureforhuman(true);
		placenearbywoods(start);
		Squad.active.x = start.x;
		Squad.active.y = start.y;
		Squad.active.displace();
		Squad.active.place();
		Squad.active.equipment.fill(Squad.active);
		Squad.active.lasttown = start;
		// Outpost.discover(start.x, start.y, Outpost.VISIONRANGE);
	}

	// static void placemoretowns() {
	// int more = RPG.r(5, 7);
	// while (more > 0) {
	// int x = RPG.r(0, World.SIZE - 1);
	// int y = RPG.r(0, World.SIZE - 1);
	// if (World.get(x, y) != null || !World.seed.map[x][y]
	// .generatetown(new Point(x, y), World.seed)) {
	// continue;
	// }
	// more -= 1;
	// Point p = new Point(x, y);
	// Town t = new Town(p, determinecolor(p).realm);
	// // while (t.isnear(Town.class, Town.MINIMUMDISTANCE)) {
	// // Location.generate(t, false);
	// // }
	// t.place();
	// }
	// }

	public static void placenearbywoods(Town start) {
		int x, y;
		int minx = start.x - Outpost.VISIONRANGE;
		int maxx = start.x + Outpost.VISIONRANGE;
		int miny = start.y - Outpost.VISIONRANGE;
		int maxy = start.y + Outpost.VISIONRANGE;
		for (x = minx; x <= maxx; x++) {
			for (y = miny; y <= maxy; y++) {
				if (World.validatecoordinate(x, y)
						&& World.seed.map[x][y].equals(Terrain.FOREST)) {
					return; // already has nearby woods
				}
			}
		}
		x = -1;
		y = -1;
		while (!World.validatecoordinate(x, y)
				|| !World.validatecoordinate(x + 1, y)
				|| World.get(x, y) != null || World.get(x + 1, y) != null) {
			x = RPG.r(minx, maxx);
			y = RPG.r(miny, maxy);
		}
		World.seed.map[x][y] = Terrain.FOREST;
		World.seed.map[x + 1][y] = Terrain.FOREST;
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
		if (World.get(x, y) != null) {
			return true;
		}
		ArrayList<Actor> towns = World.getall(Town.class);
		if (townbufferenabled) {
			for (final Actor p : towns) {
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
			for (final Actor p : towns) {
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
	public void bumpretry() {
		retries += 1;
		if (retries > MAXRETRIES) {
			retries = 0;
			synchronized (this) {
				discarded += 1;
			}
			throw new RestartWorldGeneration();
		}
	}

	public static void retry() {
		Thread t = Thread.currentThread();
		if (t instanceof WorldGenerator) {
			if (World.seed != null) {
				throw new RestartWorldGeneration();
			}
			WorldGenerator builder = (WorldGenerator) t;
			builder.bumpretry();
		}
	}

	public static void generate(World w) {
		int size = World.scenario.size;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				w.map[i][j] = Terrain.FOREST;
			}
		}
		LinkedList<Realm> realms = new LinkedList<Realm>();
		for (Realm r : Realm.values()) {
			realms.add(r);
		}
		Collections.shuffle(realms);
		ArrayList<List<Point>> regions = new ArrayList<List<Point>>(
				WorldGenerator.GENERATIONORDER.length);
		for (Terrain t : WorldGenerator.GENERATIONORDER) {
			regions.add(t.generate(w));
		}
		Point nw = new Point(0, 0);
		Point sw = new Point(0, size - 1);
		Point se = new Point(size - 1, size - 1);
		Point ne = new Point(size - 1, 0);
		floodedge(nw, sw, +1, 0, w);
		floodedge(sw, se, 0, -1, w);
		floodedge(ne, se, -1, 0, w);
		floodedge(nw, ne, 0, +1, w);
		generatetowns(realms, regions);
	}

	static void generatetowns(LinkedList<Realm> realms,
			ArrayList<List<Point>> regions) {
		int towns = World.scenario.towns;
		for (int i = 0; i < regions.size() && towns > 0; i++) {
			Terrain t = WorldGenerator.GENERATIONORDER[i];
			if (!t.equals(Terrain.WATER)) {
				new Town(regions.get(i), realms.pop()).place();
				towns -= 1;
			}
		}
	}

	private static void floodedge(Point from, Point to, int deltax, int deltay,
			World w) {
		ArrayList<Point> edge = new ArrayList<Point>(World.scenario.size);
		edge.add(from);
		edge.add(to);
		if (from.x != to.x) {
			for (int x = from.x + 1; x != to.x; x++) {
				edge.add(new Point(x, from.y));
			}
		} else {
			for (int y = from.y + 1; y != to.y; y++) {
				edge.add(new Point(from.x, y));
			}
		}
		final Terrain[][] map = w.map;
		for (Point p : edge) {
			map[p.x][p.y] = Terrain.WATER;
			if (RPG.random() <= .5f) {
				map[p.x + deltax][p.y + deltay] = Terrain.WATER;
				if (RPG.random() <= .33f) {
					map[p.x + deltax * 2][p.y + deltay * 2] = Terrain.WATER;
				}
			}
		}
	}

	public static void build() {
		int threads = Math.max(1, Preferences.MAXTHREADS);
		if (Javelin.DEBUG && SINGLETHREAD) {
			threads = 1;
		}
		final String info = "Building world, using " + threads
				+ " thread(s)...\n\nWorlds discarded: ";
		try {
			for (int i = 0; i < threads; i++) {
				new WorldGenerator().start();
			}
			int lastdiscarded = -1;
			while (World.seed == null) {
				if (lastdiscarded != discarded) {
					new InfoScreen("").print(info + discarded);
					lastdiscarded = discarded;
				}
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
