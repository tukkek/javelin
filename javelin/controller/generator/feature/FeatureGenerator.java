package javelin.controller.generator.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.Realm;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Caravan;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.Portal;
import javelin.model.world.location.Resource;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.model.world.location.fortification.Guardian;
import javelin.model.world.location.fortification.Trove;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.governor.MonsterGovernor;
import javelin.model.world.location.town.labor.base.Dwelling;
import javelin.model.world.location.town.labor.base.Lodge;
import javelin.model.world.location.town.labor.criminal.ThievesGuild;
import javelin.model.world.location.town.labor.cultural.BardsGuild;
import javelin.model.world.location.town.labor.cultural.MagesGuild;
import javelin.model.world.location.town.labor.cultural.MagesGuild.MageGuildData;
import javelin.model.world.location.town.labor.ecological.ArcheryRange;
import javelin.model.world.location.town.labor.ecological.Henge;
import javelin.model.world.location.town.labor.ecological.MeadHall;
import javelin.model.world.location.town.labor.military.MartialAcademy;
import javelin.model.world.location.town.labor.military.Monastery;
import javelin.model.world.location.town.labor.military.RealmAcademy;
import javelin.model.world.location.town.labor.productive.Mine;
import javelin.model.world.location.town.labor.productive.Shop;
import javelin.model.world.location.town.labor.religious.Sanctuary;
import javelin.model.world.location.town.labor.religious.Shrine;
import javelin.model.world.location.unique.AdventurersGuild;
import javelin.model.world.location.unique.Artificer;
import javelin.model.world.location.unique.AssassinsGuild;
import javelin.model.world.location.unique.Haxor;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.model.world.location.unique.PillarOfSkulls;
import javelin.model.world.location.unique.SummoningCircle;
import javelin.model.world.location.unique.TrainingHall;
import javelin.model.world.location.unique.minigame.Arena;
import javelin.model.world.location.unique.minigame.Battlefield;
import javelin.model.world.location.unique.minigame.DungeonRush;
import javelin.model.world.location.unique.minigame.Ziggurat;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * Responsible for generating those {@link Actor}s (mostly {@link Location} s
 * that can be spawned both during {@link World} generation and normal gameplay.
 *
 * @author alex
 * @see WorldScreen#endturn()
 */
public class FeatureGenerator {
	/** Only access point to this class. */
	public static final FeatureGenerator SINGLETON = new FeatureGenerator();

	static final int STARTINGFEATURES = World.scenario.size
			* World.scenario.size / 5;

	final HashMap<Class<? extends Actor>, GenerationData> generators = new HashMap<Class<? extends Actor>, GenerationData>();

	/**
	 * The ultimate goal of this method is to try and make it so one feature
	 * only is generated per week. Since we want some features to have a higher
	 * chance of being spawned this deals with these cases dynamically to avoid
	 * manually-written methods from becoming too large.
	 */
	private FeatureGenerator() {
		GenerationData dungeons = new GenerationData(2f);
		Integer startingdungeons = World.scenario.startingdungeons;
		if (startingdungeons != null) {
			dungeons.seeds = startingdungeons;
			dungeons.max = startingdungeons;
		}
		register(Dungeon.class, dungeons);
		register(Outpost.class, new GenerationData());
		register(Lodge.class, new GenerationData(.75f, 5, 1));
		register(Shrine.class, new GenerationData());
		register(Resource.class, new GenerationData());
		register(Mine.class, new GenerationData(1, 2, 2));

		register(Trove.class, new GenerationData(1.5f, null, 0));
		register(Guardian.class, new GenerationData(null));
		register(Dwelling.class, new GenerationData(null));

		register(Portal.class, new GenerationData() {
			@Override
			public Actor generate(Class<? extends Actor> feature) {
				return Portal.open();
			}
		});

		if (Caravan.ALLOW) {
			register(Caravan.class,
					new GenerationData(1 / 4f, true, false)).seeds = 0;
		}
		convertchances();
	}

	/**
	 * Will convert all relative (non-absolute) {@link GenerationData#chance} to
	 * an absolute value so as to make them sum up to a 100%.
	 *
	 * @see GenerationData#absolute
	 */
	protected void convertchances() {
		float total = 0;
		for (GenerationData g : generators.values()) {
			if (!g.absolute) {
				total += g.chance;
			}
		}
		for (GenerationData g : generators.values()) {
			if (!g.absolute) {
				g.chance = g.chance / total;
			}
		}
	}

	GenerationData register(Class<? extends Actor> class1,
			GenerationData generator) {
		generators.put(class1, generator);
		return generator;
	}

	/**
	 * Spawns {@link Actor}s into the game world. Used both during world
	 * generation and during a game's progress.
	 *
	 * @param chance
	 *            Used to modify the default spawning chances. For example: if
	 *            this is called daily but the target is to spawn one feature
	 *            per week then one would provide a 1/7f value here. The default
	 *            spawning chances are calculated so as to sum up to 100% so
	 *            using a value of 1 would be likely to spawn 1 random feature
	 *            in the world map, but could spawn more or none depending on
	 *            the random number generator results.
	 * @param generatingworld
	 *            If <code>false</code> will limit spawning to only a starting
	 *            set of actors. <code>true</code> is supposed to be used while
	 *            the game is progressing to support the full feature set.
	 */
	public void spawn(float chance, boolean generatingworld) {
		if (countplaces() >= STARTINGFEATURES
				|| (!World.scenario.respawnlocations && !generatingworld)) {
			return;
		}
		for (Class<? extends Actor> feature : generators.keySet()) {
			GenerationData g = generators.get(feature);
			if (generatingworld && !g.starting) {
				continue;
			}
			if (g.max != null && World.getall(feature).size() >= g.max) {
				continue;
			}
			if (RPG.random() <= chance * g.chance) {
				g.generate(feature).place();
			}
		}
	}

	static void spawnnear(Town t, Actor a, World w, int min, int max,
			boolean clear) {
		int[] location = null;
		while (location == null
				|| World.get(t.x + location[0], t.y + location[1]) != null
				|| t.x + location[0] < 0 || t.y + location[1] < 0
				|| t.x + location[0] >= World.scenario.size
				|| t.y + location[1] >= World.scenario.size
				|| w.map[t.x + location[0]][t.y + location[1]]
						.equals(Terrain.WATER)) {
			location = new int[] { RPG.r(min, max), RPG.r(min, max) };
			if (RPG.r(1, 2) == 1) {
				location[0] = -location[0];
			}
			if (RPG.r(1, 2) == 1) {
				location[1] = -location[1];
			}
		}
		a.x = location[0] + t.x;
		a.y = location[1] + t.y;
		Location l = a instanceof Location ? (Location) a : null;
		a.place();
		if (l != null && clear) {
			l.capture();
		}
	}

	static Town gettown(Terrain terrain, World seed, ArrayList<Town> towns) {
		Collections.shuffle(towns);
		for (Town town : towns) {
			if (seed.map[town.x][town.y] == terrain) {
				return town;
			}
		}
		if (Javelin.DEBUG) {
			throw new RuntimeException("No town in terrain " + terrain);
		} else {
			throw new RestartWorldGeneration();
		}
	}

	/**
	 * @param seed
	 *            Place all world features in this seed.
	 * @return Starting town for the initial {@link Squad} to be placed nearby.
	 */
	public Town placestartingfeatures(World seed) {
		Temple.generatetemples();
		Terrain starton = RPG.r(1, 2) == 1 ? Terrain.PLAIN : Terrain.HILL;
		ArrayList<Town> towns = Town.gettowns();
		Town[] easy = getlinkedtowns(seed, starton, towns);
		Town starting = (Town) World.get(easy[0].x, easy[0].y, towns);
		if (Terrain.search(new Point(starting.x, starting.y), Terrain.WATER, 2,
				seed) != 0) {
			throw new RestartWorldGeneration();
		}
		new Portal(starting, World.get(easy[1].x, easy[1].y, towns), false,
				false, true, true, null, false).place();
		generatestartingarea(seed, easy[0]);
		generatelocations(seed, easy[0]);
		for (Class<? extends Actor> feature : generators.keySet()) {
			generators.get(feature).seed(feature);
		}
		int target = STARTINGFEATURES - Location.count();
		while (countplaces() < target) {
			spawn(1, true);
		}
		if (World.scenario.normalizemap) {
			normalizemap(towns, starting);
		}
		return starting;
	}

	void normalizemap(ArrayList<Town> towns, Town starting) {
		towns = new ArrayList<Town>(towns);
		towns.remove(starting);
		Realm r = towns.get(0).originalrealm;
		for (Actor a : World.getall()) {
			if (a instanceof Location) {
				Location l = (Location) a;
				if (l.realm != null) {
					l.realm = r;
					if (a instanceof Town) {
						Town t = (Town) a;
						t.originalrealm = r;
						t.replacegovernor(new MonsterGovernor(t));
					}
				}
			}
		}
	}

	Town[] getlinkedtowns(World w, Terrain t, ArrayList<Town> towns) {
		if (World.scenario.linktowns) {
			Town a = gettown(t, w, towns);
			Town b = gettown(t == Terrain.PLAIN ? Terrain.HILL : Terrain.PLAIN,
					w, towns);
			return new Town[] { a, b };
		} else {
			Collections.shuffle(towns);
			return new Town[] { towns.get(0), towns.get(1) };
		}
	}

	void generatelocations(World seed, Town easya) {
		ArrayList<Location> locations = new ArrayList<Location>();
		generateuniquelocations(locations);
		UpgradeHandler.singleton.gather();
		generatemageguilds(locations);
		generatemartialacademies(locations);
		Collections.shuffle(locations);
		int place = Math.min(locations.size(),
				STARTINGFEATURES / 3 - countplaces());
		for (int i = 0; i < place; i++) {
			Location l = locations.get(i);
			l.place();
		}
	}

	void generateuniquelocations(ArrayList<Location> locations) {
		locations.addAll(Arrays.asList(new Location[] { new MercenariesGuild(),
				new Artificer(), new SummoningCircle(), new PillarOfSkulls(),
				new Arena(), new Battlefield(), new DungeonRush(),
				new Ziggurat() }));
	}

	void generatestartingarea(World seed, Town t) {
		spawnnear(t, new Lodge(), seed, 1, 2, true);
		spawnnear(t, new Shop(true, t.realm), seed, 1, 2, true);
		spawnnear(t, new RealmAcademy(t.originalrealm), seed, 1, 2, true);
		ArrayList<Monster> recruits = t.getpossiblerecruits();
		recruits.sort(new Comparator<Monster>() {
			@Override
			public int compare(Monster o1, Monster o2) {
				float difference = o1.challengerating - o2.challengerating;
				if (difference == 0) {
					return 0;
				}
				return difference > 0 ? 1 : -1;
			}
		});
		Haxor.singleton = new Haxor();
		spawnnear(t, Haxor.singleton, seed, 2, 3, true);
		spawnnear(t, new Dwelling(recruits.get(RPG.r(1, 7))), seed, 1, 2, true);
		spawnnear(t, new AdventurersGuild(), seed, 2, 3, true);
		spawnnear(t, new TrainingHall(), seed, 2, 3, false);
	}

	static void generatemartialacademies(ArrayList<Location> locations) {
		for (javelin.model.world.location.town.labor.military.MartialAcademy.MartialAcademyData g : MartialAcademy.GUILDS) {
			locations.add(g.generate());
		}
		locations.addAll(Arrays.asList(new Location[] { new ArcheryRange(),
				new MeadHall(), new AssassinsGuild(), new Henge(),
				new BardsGuild(), new ThievesGuild(), new Monastery(),
				new Sanctuary() }));

	}

	static int countplaces() {
		int count = 0;
		for (ArrayList<Actor> instances : World.getseed().actors.values()) {
			if (instances.isEmpty()
					|| !(instances.get(0) instanceof Location)) {
				continue;
			}
			count += instances.size();
		}
		return count;
	}

	static void generatemageguilds(ArrayList<Location> locations) {
		for (MageGuildData g : MagesGuild.GUILDS) {
			locations.add(g.generate());
		}
	}
}
