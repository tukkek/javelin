package javelin.controller.generator.feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javelin.controller.Point;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.scenario.Scenario;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.Realm;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.world.Actor;
import javelin.model.world.Caravan;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.Resource;
import javelin.model.world.location.WildEvent;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.fortification.Guardian;
import javelin.model.world.location.fortification.RealmAcademy;
import javelin.model.world.location.fortification.Trove;
import javelin.model.world.location.haunt.AbandonedManor;
import javelin.model.world.location.haunt.Graveyard;
import javelin.model.world.location.haunt.Haunt;
import javelin.model.world.location.haunt.OrcSettlement;
import javelin.model.world.location.haunt.ShatteredTemple;
import javelin.model.world.location.haunt.SunkenShip;
import javelin.model.world.location.haunt.WitchesHideout;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.governor.MonsterGovernor;
import javelin.model.world.location.town.labor.basic.Dwelling;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.model.world.location.town.labor.criminal.ThievesGuild;
import javelin.model.world.location.town.labor.cultural.BardsGuild;
import javelin.model.world.location.town.labor.cultural.MagesGuild;
import javelin.model.world.location.town.labor.cultural.MagesGuild.MageGuildData;
import javelin.model.world.location.town.labor.ecological.ArcheryRange;
import javelin.model.world.location.town.labor.ecological.Henge;
import javelin.model.world.location.town.labor.ecological.MeadHall;
import javelin.model.world.location.town.labor.military.MartialAcademy;
import javelin.model.world.location.town.labor.military.MartialAcademy.MartialAcademyData;
import javelin.model.world.location.town.labor.military.Monastery;
import javelin.model.world.location.town.labor.productive.Shop;
import javelin.model.world.location.town.labor.religious.Sanctuary;
import javelin.model.world.location.town.labor.religious.Shrine;
import javelin.model.world.location.unique.AdventurersGuild;
import javelin.model.world.location.unique.Artificer;
import javelin.model.world.location.unique.AssassinsGuild;
import javelin.model.world.location.unique.DeepDungeon;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.model.world.location.unique.PillarOfSkulls;
import javelin.model.world.location.unique.SummoningCircle;
import javelin.model.world.location.unique.TrainingHall;
import javelin.model.world.location.unique.minigame.Arena;
import javelin.model.world.location.unique.minigame.Battlefield;
import javelin.model.world.location.unique.minigame.Ziggurat;
import javelin.old.RPG;

/**
 * Responsible for generating those {@link Actor}s (mostly {@link Location}s
 * that can be spawned both during {@link World} generation and normal gameplay.
 *
 * @author alex
 */
public class FeatureGenerator implements Serializable {
	final HashMap<Class<? extends Actor>, GenerationData> generators = new HashMap<>();

	/**
	 * The ultimate goal of this method is to try and make it so one feature
	 * only is generated per week. Since we want some features to have a higher
	 * chance of being spawned this deals with these cases dynamically to avoid
	 * manually-written methods from becoming too large.
	 */
	void init() {
		GenerationData dungeons = new GenerationData(2f);
		Integer startingdungeons = World.scenario.startingdungeons;
		if (startingdungeons != null) {
			dungeons.seeds = startingdungeons;
			dungeons.max = startingdungeons;
		}
		register(Dungeon.class, dungeons);
		register(Outpost.class, new GenerationData(.25f, null, 0));
		register(Resource.class, new GenerationData());

		register(Trove.class, new GenerationData(null));
		register(Guardian.class, new GenerationData(null));
		register(Dwelling.class, new GenerationData(null));

		register(WildEvent.class, new GenerationData(2f)); // TODO

		if (World.scenario.worlddistrict) {
			register(Lodge.class, new GenerationData(.75f, 5, 1));
			register(Shrine.class, new GenerationData());
		}

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
	void convertchances() {
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
	 * @see GenerationData#starting
	 */
	public void spawn(float chance, boolean generatingworld) {
		if (countplaces() >= World.scenario.startingfeatures
				|| !World.scenario.respawnlocations && !generatingworld) {
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

	void spawnnear(Town t, Actor a, World w, int min, int max, boolean clear) {
		Point p = null;
		ArrayList<Actor> actors = World.getactors();
		while (p == null || World.get(t.x + p.x, t.y + p.y, actors) != null
				|| !World.validatecoordinate(t.x + p.x, t.y + p.y)
				|| w.map[t.x + p.x][t.y + p.y].equals(Terrain.WATER)) {
			p = new Point(RPG.r(min, max), RPG.r(min, max));
			if (RPG.chancein(2)) {
				p.x *= -1;
			}
			if (RPG.chancein(2)) {
				p.y *= -1;
			}
		}
		a.x = p.x + t.x;
		a.y = p.y + t.y;
		Location l = a instanceof Location ? (Location) a : null;
		a.place();
		if (l != null && clear) {
			l.capture();
		}
	}

	void generatelocations(World w) {
		ArrayList<Location> locations = new ArrayList<>();
		generateuniquelocations(locations);
		UpgradeHandler.singleton.gather();
		if (World.scenario.worlddistrict) {
			generatemageguilds(locations);
			generateacademies(locations);
		}
		locations.addAll(World.scenario.generatelocations(w));
		Collections.shuffle(locations);
		int place = Math.min(locations.size(),
				World.scenario.startingfeatures / 3 - countplaces());
		for (int i = 0; i < place; i++) {
			Location l = locations.get(i);
			l.place();
		}
	}

	void generateuniquelocations(ArrayList<Location> locations) {
		locations.addAll(Arrays.asList(new Location[] { new PillarOfSkulls(),
				new Arena(), new Battlefield(), new Ziggurat(),
				new DeepDungeon() }));
		if (World.scenario.worlddistrict) {
			locations.addAll(
					Arrays.asList(new Location[] { new MercenariesGuild(),
							new Artificer(), new SummoningCircle(5, 15), }));
		}
		locations.addAll(Arrays.asList(new Haunt[] { new AbandonedManor(),
				new SunkenShip(), new ShatteredTemple(), new WitchesHideout(),
				new Graveyard(), new OrcSettlement() }));
	}

	void generatestartingarea(World seed, Town t) {
		spawnnear(t, new Lodge(), seed, 1, 2, true);
		spawnnear(t, new Shop(t.realm, true), seed, 1, 2, true);
		RealmAcademy academy = new RealmAcademy(t.originalrealm, true);
		spawnnear(t, academy, seed, 1, 2, true);
		ArrayList<Monster> recruits = t.getpossiblerecruits();
		recruits.sort(new Comparator<Monster>() {
			@Override
			public int compare(Monster o1, Monster o2) {
				float difference = o1.cr - o2.cr;
				if (difference == 0) {
					return 0;
				}
				return difference > 0 ? 1 : -1;
			}
		});
		spawnnear(t, new Dwelling(recruits.get(RPG.r(1, 7))), seed, 1, 2, true);
		spawnnear(t, new AdventurersGuild(), seed, 2, 3, true);
		spawnnear(t, new TrainingHall(), seed, 2, 3, false);
	}

	void generateacademies(ArrayList<Location> locations) {
		for (MartialAcademyData g : MartialAcademy.ACADEMIES) {
			locations.add(g.generate());
		}
		locations.addAll(Arrays.asList(new Academy[] { new ArcheryRange(),
				new MeadHall(), new AssassinsGuild(), new Henge(),
				new BardsGuild(), new ThievesGuild(), new Monastery(),
				new Sanctuary() }));
		for (Discipline d : Discipline.DISCIPLINES) {
			if (d.hasacademy) {
				locations.add(d.generateacademy());
			}
		}
	}

	int countplaces() {
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

	void generatemageguilds(ArrayList<Location> locations) {
		for (MageGuildData g : MagesGuild.GUILDS) {
			locations.add(g.generate());
		}
	}

	/**
	 * Starts generating the inital state for this {@link World}.
	 *
	 * TODO parameters should be a Map instead of 2 lists
	 *
	 * @param realms
	 *            Shuffled list of realms.
	 * @param regions
	 *            Each area in the world, in the same order as the realms.
	 *
	 * @see Terrain
	 */
	public Town generate(LinkedList<Realm> realms,
			ArrayList<HashSet<Point>> regions, World w) {
		generatetowns(realms, regions);
		Town starting = determinestartingtown(w);
		normalizemap(starting);
		generatefeatures(w, starting);
		normalizemap(starting);
		for (Town t : Town.gettowns()) {
			if (t != starting) {
				t.populategarisson();
			}
		}
		return starting;
	}

	void generatefeatures(World w, Town starting) {
		init();
		Temple.generatetemples();
		generatestartingarea(w, starting);
		generatelocations(w);
		for (Class<? extends Actor> feature : generators.keySet()) {
			generators.get(feature).seed(feature);
		}
		int target = World.scenario.startingfeatures - Location.count();
		while (countplaces() < target) {
			spawn(1, true);
		}
	}

	static Town gettown(Terrain terrain, World seed, ArrayList<Town> towns) {
		Collections.shuffle(towns);
		for (Town town : towns) {
			if (seed.map[town.x][town.y] == terrain) {
				return town;
			}
		}
		throw new RestartWorldGeneration();
		/*
		 * TODO there is a bug that is allowing the generation to fall here,
		 * debug when it happens and make sure towns are being generated
		 * properly
		 */
	}

	Town determinestartingtown(World seed) {
		Terrain starton = RPG.r(1, 2) == 1 ? Terrain.PLAIN : Terrain.HILL;
		ArrayList<Town> towns = Town.gettowns();
		Town starting = World.scenario.easystartingtown
				? gettown(starton, seed, towns)
				: RPG.pick(towns);
		if (Terrain.search(new Point(starting.x, starting.y), Terrain.WATER, 2,
				seed) != 0) {
			throw new RestartWorldGeneration();
		}
		return starting;
	}

	/**
	 * Turn whole map into 2 {@link Realm}s only so that there won't be
	 * in-fighting between hostile {@link Town}s.
	 *
	 * @param starting
	 *
	 * @see Scenario#normalizemap
	 */
	void normalizemap(Town starting) {
		if (!World.scenario.normalizemap) {
			return;
		}
		ArrayList<Town> towns = Town.gettowns();
		towns.remove(starting);
		Realm r = towns.get(0).originalrealm;
		for (Actor a : World.getactors()) {
			Location l = a instanceof Location ? (Location) a : null;
			if (l != null && l.realm != null) {
				l.realm = r;
				if (a instanceof Town) {
					Town t = (Town) a;
					t.originalrealm = r;
					t.replacegovernor(new MonsterGovernor(t));
				}
			}
		}
	}

	void generatetowns(LinkedList<Realm> realms,
			ArrayList<HashSet<Point>> regions) {
		int towns = World.scenario.towns;
		for (int i = 0; i < regions.size() && towns > 0; i++) {
			Terrain t = WorldGenerator.GENERATIONORDER[i];
			if (!t.equals(Terrain.WATER)) {
				new Town(regions.get(i), realms.pop()).place();
				towns -= 1;
			}
		}
	}
}
