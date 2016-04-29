package javelin.model.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import javelin.JavelinApp;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.walker.Walker;
import javelin.model.BattleMap;
import javelin.model.Realm;
import javelin.model.world.place.Lair;
import javelin.model.world.place.Outpost;
import javelin.model.world.place.Portal;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.place.dungeon.Dungeon;
import javelin.model.world.place.guarded.Academy;
import javelin.model.world.place.guarded.Dwelling;
import javelin.model.world.place.guarded.Guardian;
import javelin.model.world.place.guarded.Inn;
import javelin.model.world.place.guarded.Shrine;
import javelin.model.world.place.town.Town;
import javelin.model.world.place.unique.MercenariesGuild;
import javelin.model.world.place.unique.Artificer;
import javelin.model.world.place.unique.Haxor;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Tile;

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
	 * A region is a sector of the world with a certain game difficulty
	 * associated to it. Forests are the background. have 2 plain areas (1 is
	 * player start), 3 mountain areas and 1 swamp.
	 * 
	 * @author alex
	 */
	public enum Region {
		EASYA, EASYB, NORMALA, HARDA, HARDB, HARDC, VERYHARDA
	}

	/**
	 * 2/16 Easy (el-5 to el-8) - plains
	 */
	public static final int EASY = Tile.PLAINS;
	/**
	 * 10/16 Moderate (el-4) - forest
	 */
	public static final int MEDIUM = Tile.FORESTS;
	/**
	 * 3/16 Difficult (el-3 to el) - mountains
	 */
	public static final int HARD = Tile.HILLS;
	/**
	 * 1/16 Very difficult (el+1) - swamp
	 */
	public static final int VERYHARD = Tile.GUNK;

	/**
	 * Map size in squares.
	 */
	static public int MAPDIMENSION = 30;
	/**
	 * Randomly generated world map.
	 */
	public static World seed;

	static final int TOWNBUFFER = 1;
	static final int[] NOISE = new int[] { 0, 2, 3 };
	static final int NOISEAMOUNT = MAPDIMENSION * MAPDIMENSION / 10;

	Region[][] map = new Region[MAPDIMENSION][MAPDIMENSION];
	TreeMap<Region, Point> towns;

	static final ArrayList<Realm> TOWNINFO = new ArrayList<Realm>();
	/**
	 * The sum total of the values of this map should be 1 (100%).
	 * 
	 * Ideally one feature is spawned per week.
	 */
	public static final HashMap<Class<? extends WorldActor>, Float> FEATURECHANCE =
			new HashMap<Class<? extends WorldActor>, Float>();
	private static final int NUMBEROFSTARTINGFEATURES =
			(MAPDIMENSION * MAPDIMENSION) / 5;;

	static {
		TOWNINFO.add(Realm.FIRE);
		TOWNINFO.add(Realm.WATER);
		TOWNINFO.add(Realm.WIND);
		TOWNINFO.add(Realm.EARTH);
		TOWNINFO.add(Realm.MAGICAL);
		TOWNINFO.add(Realm.GOOD);
		TOWNINFO.add(Realm.EVIL);

		FEATURECHANCE.put(Dungeon.class, 1f / 5f);
		FEATURECHANCE.put(Portal.class, 1f / 5f);
		FEATURECHANCE.put(Lair.class, 1f / 5f);
		float special = 2 / (5f * 5f);
		FEATURECHANCE.put(Outpost.class, special);
		FEATURECHANCE.put(Inn.class, special);
		FEATURECHANCE.put(Shrine.class, Shrine.DEBUG ? 10 : special);
		FEATURECHANCE.put(Guardian.class, Guardian.DEBUG ? 10 : special);
		FEATURECHANCE.put(Dwelling.class, Dwelling.DEBUG ? 10 : special);
	}

	World() {
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

	void buildregions(final TreeMap<Region, Integer> regionsizes) {
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

	Region expand(Region r, Point p, int maxtries) {
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

	void addnoise() {
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

	Integer updatesize(final Region expand, final int i,
			final TreeMap<Region, Integer> regionsizes) {
		return regionsizes.put(expand, regionsizes.get(expand) + i);
	}

	boolean outside(final int y) {
		return y < 0 || y >= MAPDIMENSION;
	}

	int randomstep() {
		return RPG.pick(new int[] { -1, 0, +1 });
	}

	boolean isbuilding(final TreeMap<Region, Integer> regionsizes) {
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

	boolean validatetowns() {
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

	static double triangledistance(final Point a, final Point b) {
		return Math.sqrt(calcdist(a.x - b.x) + calcdist(a.y - b.y));
	}

	static int calcdist(final int deltax) {
		final int abs = Math.abs(deltax);
		return abs * abs;
	}

	TreeMap<Region, Point> placetowns() {
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

	void placetown(final TreeMap<Region, Point> towns, final Region r) {
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

	int randomaxispoint() {
		return RPG.r(1, MAPDIMENSION - 2);
	}

	int getTile(final int i, final int j) {
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

	/**
	 * Spawns {@link WorldActor}s into the game world.
	 * 
	 * @param chance
	 *            Used to modify the default spawning chances. For example: if
	 *            this is called daily but the target is to spawn one feature
	 *            per week then one would provide a 1/7f value here. The default
	 *            spawning chances are calculated so as to sum up to 100% so
	 *            using a vlue of 1 would be likely to spawn 1 random feature in
	 *            the world map, but could spawn more or none depending on the
	 *            random number generator results.
	 * @param generatingworld
	 *            If <code>false</code> will limit spawning to only a starting
	 *            set of actors. <code>true</code> is supposed to be used while
	 *            the game is progressing and support the full set.
	 */
	static public void spawnfeatures(final float chance,
			boolean generatingworld) {
		if (RPG.random() < getfeaturechance(chance, Lair.class)) {
			new Lair().place();
		}
		if (RPG.random() < getfeaturechance(chance, Dungeon.class)) {
			new Dungeon().place();
		}
		if (RPG.random() < getfeaturechance(chance, Outpost.class)) {
			new Outpost().place();
		}
		if (RPG.random() < getfeaturechance(chance, Inn.class)) {
			new Inn().place();
		}
		if (RPG.random() < getfeaturechance(chance, Shrine.class)) {
			new Shrine().place();
		}
		if (RPG.random() < getfeaturechance(chance, Guardian.class)) {
			new Guardian().place();
		}
		if (RPG.random() < getfeaturechance(chance, Dwelling.class)) {
			new Dwelling().place();
		}
		Portal.open(chance * FEATURECHANCE.get(Portal.class));
		if (!generatingworld) {
			if (RPG.random() < chance / 4f) {
				new Merchant().place();
			}
		}
	}

	static float getfeaturechance(final float onefeatureperweek, Object type) {
		return onefeatureperweek * FEATURECHANCE.get(type);
	}

	static WorldActor determinecolor(Point p) {
		ArrayList<WorldActor> towns = WorldPlace.getall(Town.class);
		WorldActor closest = towns.get(0);
		for (int i = 1; i < towns.size(); i++) {
			WorldActor t = towns.get(i);
			if (Walker.distance(t.x, t.y, p.x, p.y) < Walker.distance(closest.x,
					closest.y, p.x, p.y)) {
				closest = t;
			}
		}
		return closest;
	}

	/**
	 * @param seed
	 *            Given an already generated seed...
	 * @return creates a view component that reflects it.
	 */
	public static BattleMap makemap(final World seed) {
		WorldScreen.worldmap = new BattleMap(MAPDIMENSION, MAPDIMENSION);
		for (int i = 0; i < WorldScreen.worldmap.width; i++) {
			for (int j = 0; j < WorldScreen.worldmap.height; j++) {
				WorldScreen.worldmap.setTile(i, j, seed.getTile(i, j));
			}
		}
		WorldScreen.worldmap.makeAllInvisible();
		Point t = seed.towns.get(Region.EASYA);
		for (int x = t.x - 2; x <= t.x + 2; x++) {
			for (int y = t.y - 2; y <= t.y + 2; y++) {
				if (x < 0 || x >= MAPDIMENSION || y < 0 || y >= MAPDIMENSION) {
					continue;
				}
				WorldScreen.setVisible(x, y);
			}
		}
		return WorldScreen.worldmap;
	}

	static Town placefeatures(final World seed) {
		Point easya = seed.towns.get(Region.EASYA);
		Point easyb = seed.towns.get(Region.EASYB);
		ArrayList<WorldActor> towns = WorldPlace.getall(Town.class);
		WorldActor startingtown = WorldScreen.getactor(easya.x, easya.y, towns);
		new Portal(startingtown, WorldScreen.getactor(easyb.x, easyb.y, towns),
				false, false, true, true, null, false).place();
		Haxor.spawn(easya);
		new MercenariesGuild().place();
		new Artificer().place();
		UpgradeHandler.singleton.gather();
		new Academy(UpgradeHandler.singleton.shots).place();
		new Academy(UpgradeHandler.singleton.expertise).place();
		new Academy(UpgradeHandler.singleton.power).place();
		int target = NUMBEROFSTARTINGFEATURES - WorldPlace.count();
		while (countplaces() < target) {
			spawnfeatures(1, true);
		}
		return (Town) startingtown;
	}

	static int countplaces() {
		int count = 0;
		for (ArrayList<WorldActor> instances : WorldActor.INSTANCES.values()) {
			if (instances.isEmpty()
					|| !(instances.get(0) instanceof WorldPlace)) {
				continue;
			}
			count += instances.size();
		}
		return count;
	}

	/**
	 * Create a map at the start of the game.
	 * 
	 * @return Starting point (Town)
	 */
	public static void makemap() {
		seed = new World();
		JavelinApp.overviewmap = World.makemap(seed);
		for (final Point town : seed.towns.values()) {
			Realm r = RPG.pick(TOWNINFO);
			TOWNINFO.remove(r);
			new Town(town.x, town.y, r).place();
		}
		int more = RPG.r(5, 7);
		for (int i = 0; i < more; i++) {
			int x = RPG.r(0, MAPDIMENSION - 1);
			int y = RPG.r(0, MAPDIMENSION - 1);
			if (WorldScreen.getactor(x, y) != null) {
				i -= 1;
				continue;
			}
			Point p = new Point(x, y);
			Town t = new Town(p.x, p.y, World.determinecolor(p).realm);
			while (t.iscloseto(Town.class)) {
				Town.generate(t);
			}
			t.place();
		}
		final Town start = World.placefeatures(seed);
		Squad.active.x = start.x;
		Squad.active.y = start.y;
		Squad.active.displace();
		Squad.active.place();
		Squad.active.equipment.fill(Squad.active);
		Squad.active.lasttown = start;
		Outpost.discover(start.x, start.y, Outpost.VISIONRANGE);
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
