package javelin.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import javelin.Javelin;
import javelin.controller.db.Preferences;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.terrain.Terrain;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.view.screen.InfoScreen;
import tyrant.mikera.engine.RPG;

public class WorldGenerator extends Thread {
	private static final boolean SINGLETHREAD = false;
	private static final int MAXRETRIES = 100000;
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
			LinkedList<Realm> realms = new LinkedList<Realm>();
			for (Realm r : Realm.values()) {
				realms.add(r);
			}
			Collections.shuffle(realms);
			ArrayList<HashSet<Point>> regions = new ArrayList<HashSet<Point>>(
					realms.size());
			generate(realms, regions, world);
			Town start = World.scenario.featuregenerator.generate(realms,
					regions, world);
			World.scenario.finish(world);
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
		Squad.active.x = start.x;
		Squad.active.y = start.y;
		Squad.active.displace();
		Squad.active.place();
		Squad.active.lasttown = start;
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

	public static void generate(LinkedList<Realm> realms,
			ArrayList<HashSet<Point>> regions, World w) {
		int size = World.scenario.size;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				w.map[i][j] = Terrain.FOREST;
			}
		}
		for (Terrain t : WorldGenerator.GENERATIONORDER) {
			regions.add(t.generate(w, null));
		}
		Point nw = new Point(0, 0);
		Point sw = new Point(0, size - 1);
		Point se = new Point(size - 1, size - 1);
		Point ne = new Point(size - 1, 0);
		floodedge(nw, sw, +1, 0, w);
		floodedge(sw, se, 0, -1, w);
		floodedge(ne, se, -1, 0, w);
		floodedge(nw, ne, 0, +1, w);
	}

	static void floodedge(Point from, Point to, int deltax, int deltay,
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
