package javelin.controller.scenario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.model.world.location.haunt.Haunt;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.labor.Trait;
import javelin.model.world.location.town.labor.basic.Dwelling;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.model.world.location.town.labor.cultural.MagesGuild;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.model.world.location.town.labor.productive.Shop;
import tyrant.mikera.engine.RPG;

public class DungeonWorld extends Campaign {
	public class ZoneGenerator extends FeatureGenerator {
		@Override
		public void spawn(float chance, boolean generatingworld) {
			// don't: static world
		}

		@Override
		public Town generate(LinkedList<Realm> realms,
				ArrayList<HashSet<Point>> regions, World w) {
			// generatenewareas(regions.get(realms.indexOf(Realm.EARTH)));
			Point p = new Point(World.scenario.size / 2,
					World.scenario.size / 2);
			Town t = new Town(p, Realm.FIRE);
			t.place();
			return t;
		}

		void generatenewareas(HashSet<Point> region) {
			int newareas = RPG.r(1, 4);
			List<Terrain> terrains = new ArrayList<Terrain>(
					Arrays.asList(Terrain.ALL));
			terrains.remove(Terrain.WATER);
			terrains.remove(Terrain.UNDERGROUND);
			terrains.remove(Terrain.FOREST);
			for (int i = 0; i < newareas; i++) {
				generatenewarea(RPG.pick(terrains), region.size() / newareas);
			}
		}

		void generatenewarea(Terrain t, int size) {
			size = RPG.randomize(size);
			// t.generate(null);
		}
	}

	static final HashSet<Class<?>> ALLOW = new HashSet<Class<?>>();
	static final int DISTANCE = 6;

	static {
		for (Class<?> actortype : new Class[] { Town.class, Dungeon.class,
				Lodge.class, Academy.class, Shop.class, Squad.class }) {
			ALLOW.add(actortype);
		}
	}

	public DungeonWorld() {
		size = size * 2;
		startingpopulation = 6;
		templekeys = false;
		minigames = false;
		record = false;
		respawnlocations = false;
		fogofwar = false;
		expiredungeons = true;
		worldencounters = false;
		helpfile = "Dungeon World";
		spawn = false;
		labormodifier = 0;
		featuregenerator = new ZoneGenerator();
	}

	@Override
	public boolean win() {
		if (Javelin.DEBUG) {
			return false;
		}
		for (Dungeon d : Dungeon.getdungeons()) {
			if (d.gettier() == DungeonTier.HIGHEST) {
				return false;
			}
		}
		String success = "You have cleared all major dungeons! Congratulations!";
		Javelin.message(success, true);
		return true;
	}

	@Override
	public void finish(World w) {
		if (Javelin.DEBUG) {
			return;
		}
		for (Actor a : World.getactors()) {
			if (allowactor(a)) {
				continue;
			}
			if (a instanceof Dwelling) {
				((Dwelling) a).capture();
			} else if (a instanceof Temple) {
				((Temple) a).open = true;
			} else if (a instanceof Haunt) {
				a.realm = null;
			} else {
				a.remove();
			}
		}
		Town starting = process(Town.gettowns());
		process(Dungeon.getdungeons(), starting);
	}

	boolean allowactor(Actor a) {
		for (Class<?> actortype : ALLOW) {
			if (actortype.isInstance(a)) {
				return true;
			}
		}
		return false;
	}

	Town process(ArrayList<Town> towns) {
		LinkedList<Trait> traits = new LinkedList<Trait>(Deck.TRAITS);
		Collections.shuffle(traits);
		Town starting = null;
		for (Town t : towns) {
			if (!t.ishostile()) {
				towns.remove(t);
				traits.pop().addto(t);
				place(RPG.pick(MagesGuild.GUILDS).generate(), t);
				starting = t;
				break;
			}
		}
		while (towns.size() > 3) {
			Town t = towns.get(0);
			t.remove();
			towns.remove(t);
		}
		for (Town t : towns) {
			place(new Shop(false, t.realm), t);
			t.capture();
			traits.pop().addto(t);
		}
		return starting;
	}

	void place(Location l, Town t) {
		l.setlocation(RPG.pick(t.getdistrict().getfreespaces()));
		l.place();
	}

	void process(List<Dungeon> dungeons, Town starting) {
		List<Town> towns = Town.gettowns();
		for (int i = 0; i < DungeonTier.TIERS.length; i++) {
			DungeonTier tier = DungeonTier.TIERS[i];
			Town t = i == 0 ? starting : RPG.pick(towns);
			move(dungeons, tier, t);
			towns.remove(t);
		}
	}

	void move(List<Dungeon> dungeons, DungeonTier tier, Town t) {
		for (Dungeon d : dungeons) {
			if (d.gettier() != tier) {
				continue;
			}
			d.remove();
			d.setlocation(findlocation(t));
			d.place();
		}
	}

	Point findlocation(Town t) {
		Point p = null;
		ArrayList<Actor> actors = World.getactors();
		while (p == null || !p.validate(0, 0, size, size)
				|| Terrain.get(p.x, p.y) == Terrain.WATER) {
			int x = RPG.r(t.x - DISTANCE, t.x + DISTANCE);
			int y = RPG.r(t.y - DISTANCE, t.y + DISTANCE);
			p = new Point(x, y);
			List<Point> adjacent = Arrays.asList(Point.getadjacent());
			ArrayList<Point> check = new ArrayList<Point>(adjacent);
			check.add(new Point(0, 0));
			for (Point near : check) {
				if (World.get(p.x + near.x, p.y + near.y, actors) != null) {
					p = null;
					break;
				}
			}
		}
		return p;
	}
}
