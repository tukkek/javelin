package javelin.model.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.walker.Walker;
import javelin.model.BattleMap;
import javelin.model.Realm;
import javelin.model.unit.Squad;
import javelin.model.world.location.Lair;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.Portal;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.fortification.Dwelling;
import javelin.model.world.location.fortification.Guardian;
import javelin.model.world.location.fortification.Inn;
import javelin.model.world.location.fortification.MagesGuild;
import javelin.model.world.location.fortification.MartialAcademy;
import javelin.model.world.location.fortification.Mine;
import javelin.model.world.location.fortification.Shrine;
import javelin.model.world.location.fortification.Trove;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.unique.AdventurersGuild;
import javelin.model.world.location.unique.Artificer;
import javelin.model.world.location.unique.Haxor;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.model.world.location.unique.PillarOfSkulls;
import javelin.model.world.location.unique.SummoningCircle;
import javelin.model.world.location.unique.TrainingHall;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.Point;
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
	 * The sum total of the values of this map should be 1 (100%).
	 * 
	 * Ideally one feature is spawned per week.
	 */
	public static final HashMap<Class<? extends WorldActor>, Float> FEATURECHANCE =
			new HashMap<Class<? extends WorldActor>, Float>();
	private static final int NUMBEROFSTARTINGFEATURES =
			(MAPDIMENSION * MAPDIMENSION) / 5;
	public static final int NREGIONS = 16;

	static {
		Float chance = 1 / 13f;
		FEATURECHANCE.put(Dungeon.class, 3 * chance);
		FEATURECHANCE.put(Trove.class, 2 * chance);
		FEATURECHANCE.put(Portal.class, chance);
		FEATURECHANCE.put(Lair.class, chance);
		FEATURECHANCE.put(Outpost.class, chance);
		FEATURECHANCE.put(Inn.class, chance);
		FEATURECHANCE.put(Shrine.class, chance);
		FEATURECHANCE.put(Guardian.class, chance);
		FEATURECHANCE.put(Dwelling.class, chance);
		FEATURECHANCE.put(Mine.class, chance);
	}

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

	static void spawnnear(Town t, WorldActor l, World w) {
		int[] haxor = null;
		while (haxor == null
				|| WorldActor.get(t.x + haxor[0], t.y + haxor[1]) != null
				|| t.x + haxor[0] < 0 || t.y + haxor[1] < 0
				|| t.x + haxor[0] >= MAPDIMENSION
				|| t.y + haxor[1] >= MAPDIMENSION
				|| w.map[t.x + haxor[0]][t.y + haxor[1]]
						.equals(Terrain.WATER)) {
			haxor = new int[] { RPG.r(2, 3), RPG.r(2, 3) };
			if (RPG.r(1, 2) == 1) {
				haxor[0] = -haxor[0];
			}
			if (RPG.r(1, 2) == 1) {
				haxor[1] = -haxor[1];
			}
		}
		l.x = haxor[0] + t.x;
		l.y = haxor[1] + t.y;
		l.place();
	}

	/**
	 * Spawns {@link WorldActor}s into the game world. Used both during world
	 * generation and during a game's progress.
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
		if (RPG.random() < getfeaturechance(chance, Trove.class)) {
			new Trove().place();
		}
		if (WorldActor.getall(Mine.class).size() < 2
				&& RPG.random() < getfeaturechance(chance, Mine.class)) {
			new Mine().place();
		}
		Portal.open(chance * FEATURECHANCE.get(Portal.class));
		if (!generatingworld) {
			if (RPG.random() < chance / 4f) {
				new Caravan().place();
			}
		}
	}

	static float getfeaturechance(final float onefeatureperweek, Object type) {
		return onefeatureperweek * FEATURECHANCE.get(type);
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

	static Town placefeatures(final World seed) {
		Terrain starton = RPG.r(1, 2) == 1 ? Terrain.PLAIN : Terrain.HILL;
		// starton = Terrain.PLAIN;// TODO
		Town easya = gettown(starton, seed);
		Town easyb = gettown(
				starton == Terrain.PLAIN ? Terrain.HILL : Terrain.PLAIN, seed);
		ArrayList<WorldActor> towns = Location.getall(Town.class);
		WorldActor startingtown = WorldActor.get(easya.x, easya.y, towns);
		new Portal(startingtown, WorldActor.get(easyb.x, easyb.y, towns), false,
				false, true, true, null, false).place();
		Haxor.singleton = new Haxor();
		spawnnear(easya, Haxor.singleton, seed);
		spawnnear(easya, new TrainingHall(), seed);
		spawnnear(easya, new AdventurersGuild(), seed);
		new MercenariesGuild().place();
		new Artificer().place();
		new SummoningCircle().place();
		new PillarOfSkulls().place();
		UpgradeHandler.singleton.gather();
		generatemageguilds();
		generatemartialacademies();
		int target = NUMBEROFSTARTINGFEATURES - Location.count();
		while (countplaces() < target) {
			spawnfeatures(1, true);
		}
		return (Town) startingtown;
	}

	static Town gettown(Terrain terrain, World seed) {
		ArrayList<WorldActor> towns =
				new ArrayList<WorldActor>(Location.getall(Town.class));
		Collections.shuffle(towns);
		for (WorldActor town : towns) {
			if (seed.map[town.x][town.y] == terrain) {
				return (Town) town;
			}
		}
		if (Javelin.DEBUG) {
			throw new RuntimeException("No town in terrain " + terrain);
		} else {
			throw new RestartWorldGeneration();
		}
	}

	static void generatemageguilds() {
		new MagesGuild("Compulsion guild",
				UpgradeHandler.singleton.schoolcompulsion,
				RaiseCharisma.INSTANCE).place();
		new MagesGuild("Conjuration guild",
				UpgradeHandler.singleton.schoolconjuration,
				RaiseCharisma.INSTANCE).place();
		new MagesGuild("Abjuration guild",
				UpgradeHandler.singleton.schoolabjuration,
				RaiseCharisma.INSTANCE).place();

		new MagesGuild("Healing guild", UpgradeHandler.singleton.schoolhealing,
				new RaiseWisdom()).place();
		new MagesGuild("Totem guild", UpgradeHandler.singleton.schooltotem,
				new RaiseWisdom()).place();
		new MagesGuild("Healing wounds guild",
				UpgradeHandler.singleton.schoolhealwounds, new RaiseWisdom())
						.place();
		new MagesGuild("Divination guild",
				UpgradeHandler.singleton.schooldivination, new RaiseWisdom())
						.place();

		new MagesGuild("Necromancy guild",
				UpgradeHandler.singleton.schoolnecromancy,
				RaiseIntelligence.INSTANCE).place();
		new MagesGuild("Wounding guild",
				UpgradeHandler.singleton.schoolwounding,
				RaiseIntelligence.INSTANCE).place();
		new MagesGuild("Evocation guild",
				UpgradeHandler.singleton.schoolevocation,
				RaiseIntelligence.INSTANCE).place();
		new MagesGuild("Transmutation guild",
				UpgradeHandler.singleton.schooltransmutation,
				RaiseIntelligence.INSTANCE).place();

	}

	static void generatemartialacademies() {
		new MartialAcademy(UpgradeHandler.singleton.shots,
				"Academy (shooting range)").place();
		new MartialAcademy(UpgradeHandler.singleton.expertise,
				"Academy (combat expertise)").place();
		new MartialAcademy(UpgradeHandler.singleton.power,
				"Academy (power attack)").place();
	}

	static int countplaces() {
		int count = 0;
		for (ArrayList<WorldActor> instances : WorldActor.INSTANCES.values()) {
			if (instances.isEmpty()
					|| !(instances.get(0) instanceof Location)) {
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
		Town start = null;
		while (seed == null) {
			try {
				WorldScreen.worldmap =
						new BattleMap(MAPDIMENSION, MAPDIMENSION);
				seed = new World();
				seed.generate();
				JavelinApp.overviewmap = WorldScreen.worldmap;
				placemoretowns();
				start = placefeatures(seed);
				if (Terrain.checkadjacent(new Point(start.x, start.y),
						Terrain.WATER, seed, 2) != 0) {
					throw new RestartWorldGeneration();
				}
			} catch (RestartWorldGeneration e) {
				seed = null;
				JavelinApp.overviewmap = null;
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
		WorldScreen.worldmap.makeAllInvisible();
		Outpost.discover(start.x, start.y, Outpost.VISIONRANGE);
		seed.done = true;
	}

	static void placemoretowns() {
		int more = RPG.r(5, 7);
		while (more > 0) {
			int x = RPG.r(0, MAPDIMENSION - 1);
			int y = RPG.r(0, MAPDIMENSION - 1);
			// if (seed.map[x][y] != Terrain.PLAIN) {
			// continue;// TODO
			// }
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
