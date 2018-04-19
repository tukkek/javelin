package javelin.controller.scenario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.model.world.location.haunt.Haunt;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.basic.Dwelling;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.model.world.location.town.labor.productive.Shop;
import tyrant.mikera.engine.RPG;

public class DungeonWorld extends Campaign {
	static final HashSet<Class<?>> ALLOW = new HashSet<Class<?>>();
	static final int DISTANCE = 6;

	static {
		for (Class<?> actortype : new Class[] { Town.class, Dungeon.class,
				Lodge.class, Academy.class, Shop.class, Squad.class }) {
			ALLOW.add(actortype);
		}
	}

	public DungeonWorld() {
		startingpopulation = 6;
		allowkeys = false;
		minigames = false;
		record = false;
		respawnlocations = false;
		fogofwar = false;
		expiredungeons = true;
		helpfile = "Dungeon World";
		spawn = false;
	}

	@Override
	public void finish(World w) {
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
		Town starting = null;
		for (Town t : towns) {
			if (!t.ishostile()) {
				starting = t;
				towns.remove(t);
				break;
			}
		}
		while (towns.size() > 3) {
			Town t = towns.get(0);
			t.remove();
			towns.remove(t);
		}
		for (Town t : towns) {
			if (t.ishostile()) {
				Shop shop = new Shop(false, t.realm);
				shop.setlocation(RPG.pick(t.getdistrict().getfreespaces()));
				shop.place();
				t.capture();
			}
		}
		return starting;
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

	@Override
	public boolean win() {
		for (Dungeon d : Dungeon.getdungeons()) {
			if (d.gettier() == DungeonTier.HIGHEST) {
				return false;
			}
		}
		Javelin.message("You have cleared all major dungeons! Congratulations!",
				true);
		return true;
	}
}
