package javelin.controller.scenario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.basic.Dwelling;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.model.world.location.town.labor.productive.Shop;
import tyrant.mikera.engine.RPG;

public class DungeonWorld extends Campaign {
	private static final int DISTANCE = 6;

	public DungeonWorld() {
		exploration = false;
		startingpopulation = 11;
		allowkeys = false;
		minigames = false;
		record = false;
		respawnlocations = false;
		fogofwar = false;
		expiredungeons = true;
		helpfile = "Dungeon World";
	}

	@Override
	public void finish(World w) {
		for (Actor a : World.getactors()) {
			if (a instanceof Town || a instanceof Dungeon || a instanceof Lodge
					|| a instanceof Shop || a instanceof Academy) {
				continue;
			}
			if (a instanceof Dwelling) {
				((Dwelling) a).capture();
			} else if (!(a instanceof Squad)) {
				a.remove();
			}
		}
		Town starting = process(Town.gettowns());
		process(Dungeon.getdungeons(), starting);
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
		for (Dungeon d : dungeons) {
			d.level -= 1;
		}
		List<Town> towns = Town.gettowns();
		for (int i = 0; i < DungeonTier.TIERS.length; i++) {
			DungeonTier tier = DungeonTier.TIERS[i];
			for (int j = i; j < 4; j++) {
				Dungeon d = new Dungeon(RPG.r(tier.level - 4, tier.level));
				d.place();
				dungeons.add(d);
			}
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
