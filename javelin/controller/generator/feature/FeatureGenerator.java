package javelin.controller.generator.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javelin.Javelin;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.model.unit.Squad;
import javelin.model.world.Caravan;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Lair;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.Portal;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.Temple;
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
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;

/**
 * Responsible for generating those {@link WorldActor}s (mostly {@link Location}
 * s that can be spawned both during {@link World} generation and normal
 * gameplay.
 * 
 * @author alex
 * @see WorldScreen#turn()
 */
public class FeatureGenerator {
	/** Only access point to this class. */
	public static final FeatureGenerator SINGLETON = new FeatureGenerator();

	static final int NUMBEROFSTARTINGFEATURES =
			(World.MAPDIMENSION * World.MAPDIMENSION) / 5;

	final HashMap<Class<? extends WorldActor>, FeatureGeneration> generators =
			new HashMap<Class<? extends WorldActor>, FeatureGeneration>();

	/**
	 * The ultimate goal of this method is to try and make it so one feature
	 * only is generated per week. Since we want some features to have a higher
	 * chance of being spawned this deals with these cases dynamically to avoid
	 * manually-written methods from becoming too large.
	 */
	private FeatureGenerator() {
		register(Dungeon.class,
				new FeatureGeneration(2f, Dungeon.STARTING, Dungeon.STARTING));
		register(Trove.class, new FeatureGeneration(1.5f));
		register(Lair.class, new FeatureGeneration());
		register(Outpost.class, new FeatureGeneration());
		register(Inn.class, new FeatureGeneration());
		register(Shrine.class, new FeatureGeneration());
		register(Guardian.class, new FeatureGeneration());
		register(Dwelling.class, new FeatureGeneration());
		register(Mine.class, new FeatureGeneration(1, 2, 2));

		register(Portal.class, new FeatureGeneration() {
			@Override
			public WorldActor generate(Class<? extends WorldActor> feature) {
				return Portal.open();
			}
		});

		register(Caravan.class, new FeatureGeneration(1 / 4f, true, false));
		convertchances();
	}

	/**
	 * Will convert all relative (non-absolute) {@link FeatureGeneration#chance}
	 * to an absolute value so as to make them sum up to a 100%.
	 * 
	 * @see FeatureGeneration#absolute
	 */
	protected void convertchances() {
		float total = 0;
		for (FeatureGeneration g : generators.values()) {
			if (!g.absolute) {
				total += g.chance;
			}
		}
		for (FeatureGeneration g : generators.values()) {
			if (!g.absolute) {
				g.chance = g.chance / total;
			}
		}
	}

	FeatureGeneration register(Class<? extends WorldActor> class1,
			FeatureGeneration generator) {
		generators.put(class1, generator);
		return generator;
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
	 *            using a value of 1 would be likely to spawn 1 random feature
	 *            in the world map, but could spawn more or none depending on
	 *            the random number generator results.
	 * @param generatingworld
	 *            If <code>false</code> will limit spawning to only a starting
	 *            set of actors. <code>true</code> is supposed to be used while
	 *            the game is progressing to support the full feature set.
	 */
	public void spawn(final float chance, boolean generatingworld) {
		for (Class<? extends WorldActor> feature : generators.keySet()) {
			FeatureGeneration g = generators.get(feature);
			if (generatingworld && !g.starting) {
				continue;
			}
			if (g.max != null
					&& WorldActor.getall(Mine.class).size() >= g.max) {
				continue;
			}
			if (RPG.random() <= chance * g.chance) {
				g.generate(feature).place();
			}
		}
	}

	static void spawnnear(Town t, WorldActor l, World w) {
		int[] haxor = null;
		while (haxor == null
				|| WorldActor.get(t.x + haxor[0], t.y + haxor[1]) != null
				|| t.x + haxor[0] < 0 || t.y + haxor[1] < 0
				|| t.x + haxor[0] >= World.MAPDIMENSION
				|| t.y + haxor[1] >= World.MAPDIMENSION
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

	/**
	 * @param seed
	 *            Place all world features in this seed.
	 * @return Starting town for the initial {@link Squad} to be placed nearby.
	 */
	public Town placestartingfeatures() {
		World seed = World.seed;
		Temple.generatetemples();
		Terrain starton = RPG.r(1, 2) == 1 ? Terrain.PLAIN : Terrain.HILL;
		Town easya = FeatureGenerator.gettown(starton, seed);
		Town easyb = FeatureGenerator.gettown(
				starton == Terrain.PLAIN ? Terrain.HILL : Terrain.PLAIN, seed);
		ArrayList<WorldActor> towns = Location.getall(Town.class);
		WorldActor startingtown = WorldActor.get(easya.x, easya.y, towns);
		if (Terrain.checkadjacent(new Point(startingtown.x, startingtown.y),
				Terrain.WATER, seed, 2) != 0) {
			throw new RestartWorldGeneration();
		}
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
		for (Class<? extends WorldActor> feature : generators.keySet()) {
			generators.get(feature).seed(feature);
		}
		int target = NUMBEROFSTARTINGFEATURES - Location.count();
		while (countplaces() < target) {
			SINGLETON.spawn(1, true);
		}
		return (Town) startingtown;
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
}
