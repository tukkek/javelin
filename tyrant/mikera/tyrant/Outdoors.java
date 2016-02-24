//
// Standard outdoor map locations
//
// Created based on the WoldMap terrain type
//

package tyrant.mikera.tyrant;

import javelin.controller.Point;
import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

public class Outdoors {

	public static BattleMap create(int t) {
		BattleMap m = new BattleMap(65, 65);

		m.setEntrance(Portal.create());
		m.addThing(m.getEntrance(), 32, 32);

		buildOutdoors(m, 0, 0, m.width - 1, m.height - 1, t);

		m.set("Description", "Somewhere in the Wild");
		m.set("WanderingRate", 100);
		m.set("IsHostile", 0);
		m.set("ForgetMap", 1);
		m.set("EnterMessage", "");
		m.set("Level", 1);

		return m;
	}

	public static void buildOutdoors(BattleMap m, int x1, int y1, int x2,
			int y2, int t) {
		int w = x2 - x1 + 1;
		int h = y2 - y1 + 1;
		int area = w * h;

		switch (t) {
		case Tile.PLAINS: {
			m.setTheme("plains");
			m.fillArea(x1, y1, x2, y2, m.floor());
			for (int i = 0; i < (area / (20 + RPG.d(100))); i++) {
				Point ts = m.findFreeSquare();
				m.addThing(Lib.create("bush"), ts.x, ts.y);
			}
			for (int i = 0; i < (area / 400); i++) {
				m.addThing(Lib.create("stone"), x1, y1, x2, y2);
				m.addThing(Lib.create("tree stump"), x1, y1, x2, y2);
			}
			Point ts = m.findFreeSquare(x1, y1, x2, y2);
			m.addThing(Lib.create("tree stump"), ts.x, ts.y);
			m.addThing(Lib.create("secret item"), ts.x, ts.y);

			if (RPG.d(6 + Game.hero().getStat(Skill.HERBLORE)) >= 6) {
				Thing herb = Lib.createType("IsHerb", RPG.d(10));
				int r = 7;
				int px = RPG.rspread(x1, x2 - r);
				int py = RPG.rspread(y1, y2 - r);
				for (int x = px; x < px + r; x++) {
					for (int y = py; y < py + r; y++) {
						if ((RPG.d(5) == 1) && (m.getTile(x, y) == m.floor())) {
							m.addThing(herb.cloneType(), x, y);
						}
					}
				}
			}

			break;
		}

		case Tile.HILLS: {
			m.setTheme("plains");
			m.fillArea(x1, y1, x2, y2, m.floor());
			for (int i = 0; i < (area / 10); i++) {
				m.setTile(x1 + RPG.r(w), y1 + RPG.r(h), Tile.HILLS);
			}
			for (int i = 0; i < (area / 40); i++) {
				Point ts = m.findFreeSquare(x1, y1, x2, y2);
				m.addThing(Lib.create("pine tree"), ts.x, ts.y);
			}
			for (int i = 0; i < (area / 80); i++) {
				Point ts = m.findFreeSquare(x1, y1, x2, y2);
				m.addThing(Lib.create("tree stump"), ts.x, ts.y);
			}
			for (int i = 0; i < (area / 80); i++) {
				Point ts = m.findFreeSquare(x1, y1, x2, y2);
				m.addThing(Lib.create("pebble"), ts.x, ts.y);
			}
			for (int i = 0; i < (area / 80); i++) {
				Point ts = m.findFreeSquare(x1, y1, x2, y2);
				m.addThing(Lib.create("rock"), ts.x, ts.y);
			}
			if (RPG.d(10) == 1) {
				m.addThing(Portal.create("dungeon"));
			}
			break;
		}

		case Tile.MOUNTAINS: {
			m.setTheme("plains");
			m.fillArea(x1, y1, x2, y2, m.floor());

			for (int i = 0; i < 10; i++) {
				int x = RPG.r(w);

				int y = RPG.r(h);

				m.spray(x - 6, y - 6, x + 6, y + 6, Tile.MOUNTAINS, 20);
			}
			break;
		}

		case Tile.SWAMP: {
			m.setTheme("swamp");
			m.fillArea(x1, y1, x2, y2, m.floor());

			for (int lx = x1; lx <= x2; lx += 8) {
				for (int ly = y1; ly <= y2; ly += 8) {
					int r = RPG.pick(
							new int[] { Tile.PLAINS, m.floor(), m.floor() });
					m.setTile(lx, ly, r);
				}
			}
			m.fractalize(x1, y1, x2, y2, 8);

			// for (int i=0; i<(area/10); i++) {
			// int px=x1+RPG.r(w);
			// int py=y1+RPG.r(h);
			// map.setTile(px,py,Tile.CAVEFLOOR);
			// }
			for (int i = 0; i < (area / 40); i++) {
				Point ts = m.findFreeSquare(x1, y1, x2, y2);
				m.addThing(Lib.create("thorny bush"), ts.x, ts.y);
			}
			break;
		}

		case Tile.FORESTS: {
			m.setTheme("woods");
			m.fillArea(x1, y1, x2, y2, m.floor());
			for (int i = 0; i < (area / 20); i++) {
				Point ts = m.findFreeSquare(x1, y1, x2, y2);
				m.addThing(Lib.create("tree"), ts.x, ts.y);
			}
			for (int i = 0; i < (area / 80); i++) {
				Point ts = m.findFreeSquare(x1, y1, x2, y2);
				m.addThing(Lib.create("tree stump"), ts.x, ts.y);
			}

			if (RPG.d(6 + Game.hero().getStat(Skill.FARMING)) >= 6) {
				m.incStat("Level", RPG.r(6));
				String st = "mushroom";
				switch (RPG.d(6)) {
				case 1:
					st = "plant";
					break;

				case 2:
					st = "[IsHerb]";
					break;

				case 3:
					st = "mushroom";
					break;

				case 4:
					st = "[IsMushroom]";
					break;

				}

				for (int i = 0; i < (area / (1 + RPG.d(100))); i++) {

					Point ts = m.findFreeSquare(x1, y1, x2, y2);
					m.addThing(st, ts.x, ts.y);
					m.incStat("WanderingRate", 1);
				}
				m.addThing("tree monster", x1, y1, x2, y2);
			}
			break;
		}

		case Tile.GRASS: {
			m.fillArea(x1, y1, x2, y2, t);
			break;
		}

		default: {
			throw new Error("Outdoor type " + t + "not recognised");

		}
		}

		{
			Point ts = m.findFreeSquare(x1, y1, x2, y2);
			addBeasties(m, ts.x, ts.y);
		}

		// add animals
		int animals = RPG.r(2 + Game.hero().getStat(Skill.TRACKING) * 2);
		for (int i = 0; i < animals; i++) {
			m.addThing(Animal.create(m.getLevel()));
		}

	}

	// add outdoor critters
	public static void addBeasties(BattleMap map, int x, int y) {
		Thing m = Lib.createCreature(map.getLevel());
		for (int i = RPG.d(m.getStat("GroupNumber")); i > 0; i--) {
			int bx = x - 2 + RPG.r(5);
			int by = y - 2 + RPG.r(5);
			if (map.isClear(bx, by)) {
				map.addThing(m.cloneType(), bx, by);
			}
		}
	}
}