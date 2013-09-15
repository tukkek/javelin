package tyrant.mikera.tyrant;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * Implements critter-infested deep forests and the hidden wood temple areas
 */
class DeepForest {

	public static BattleMap create(int w, int h) {
		BattleMap m = new BattleMap(w, h);
		makeDeepForest(m);
		return m;
	}

	public static void makeWoodTemple(BattleMap m) {
		int w = m.getWidth();
		int h = m.getHeight();

		m.setTheme("deepforest");
		m.set("WanderingRate", 0);
		m.set("IsHostile", 0);
		m.set("Level", 9);
		m.set("Description", "Wood Temple");

		m.fillArea(0, 0, w - 1, h - 1, m.wall());
		m.fillArea(1, 1, w - 2, h - 1, 0);

		// entrance centre - top
		m.setEntrance(Portal.create());
		m.addThing(m.getEntrance(), w / 2, 0);
		m.setTile(w / 2, 0, m.floor());
		makePath(m, w / 2, 1, w / 2, h / 2);
		makePath(m, 1, 20, w - 2, 20);

		// large clearing
		m.fillOval(10, h / 2, w - 10, h - 5, m.floor());

		// paths and small clearings
		for (int loop = 0; loop < 20; loop++) {
			Point ls = m.findFreeSquare();
			int cx = RPG.r(w - 2) + 1;
			int cy = RPG.r(h - 2) + 1;
			if (RPG.d(3) == 1) {
				makeClearing(m, cx, cy, false);

			}
			makePath(m, ls.x, ls.y, cx, cy);
			m.setTile(cx, cy, m.floor());
			if (RPG.d(6) == 1) {
				m.addThing(Lib.createItem(RPG.d(10)), cx, cy);
			}
		}

		// make a stream
		int sy = 20;
		m.fillArea(0, sy - RPG.r(2), 0, sy + RPG.d(2), Tile.SEA);
		for (int sx = 1; sx < w - 1; sx++) {
			sy = sy + RPG.r(3) - 1;
			if (sy < 4) {
				sy++;
			}
			if (sy > h / 2) {
				sy--;
			}
			m.fillArea(sx, sy - RPG.d(3), sx, sy + RPG.d(3), Tile.MOSSFLOOR);
			m.fillArea(sx, sy - RPG.r(2), sx, sy + RPG.d(2), Tile.STREAM);
		}
		m.fillArea(w - 1, sy - RPG.r(2), w - 1, sy + RPG.d(2), Tile.SEA);

		// guaranteed path
		makePath(m, w / 2, 1, w / 2, h - 2);

		// circle
		int cx = w / 2;
		int cy = h * 3 / 4;
		makeTempleCircle(m, cx, cy);

		// features
		makeTempleFeature(m, cx - 20, cy);
		makeTempleFeature(m, cx - 11, cy + 5);
		makeTempleFeature(m, cx - 11, cy - 7);
		makeTempleFeature(m, cx - 6, cy - 13);
		makeTempleFeature(m, cx + 2, cy - 11);
		makeTempleFeature(m, cx + 12, cy - 10);
		makeTempleFeature(m, cx + 16, cy - 3);
		makeTempleFeature(m, cx + 14, cy + 5);

		// replace tiles
		m.replaceTiles(0, m.wall());
	}

	public static void makeTempleFeature(BattleMap m, int x, int y) {
		m.clearArea(x - 3, y - 3, x + 3, y + 3);
		switch (RPG.d(4)) {
		case 1: {
			// 8 blocks grid
			m.fillArea(x - 1, y - 2, x + 1, y + 2, m.wall());
			m.fillArea(x - 2, y - 1, x + 2, y + 1, m.wall());
			m.fillArea(x - 2, y - 0, x + 2, y + 0, m.floor());
			m.fillArea(x - 1, y - 1, x + 1, y + 1, m.floor());
			m.fillArea(x - 0, y - 2, x + 0, y + 2, m.floor());
			Thing t = Lib.create("wood temple guard");
			m.addThing(t, x, y);
			AI.setGuard(t, m, x - 4, y - 4, x + 4, y + 4);
			break;
		}
		case 2: {
			// single big tree
			m.fillArea(x - 1, y - 2, x + 1, y + 2, m.wall());
			m.fillArea(x - 2, y - 1, x + 2, y + 1, m.wall());
			Thing t = Lib.create("wood temple archer");
			m.addThing(t, x - 2, y - 2);
			AI.setGuard(t, m, x - 4, y - 4, x + 4, y + 4);
			break;
		}
		default: {
			// m.fillArea(x-4,y-4,x+4,y+4,m.floortile());
			makeTreeHouse(m, x - 3, y - 2, x + 3, y + 2);
			m.setTile(x + RPG.r(3) - 1, y - 2, m.floor());
			Thing t = Lib.create("woodsman");
			m.addThing(t, x, y);
			AI.setGuard(t, m, x - 5, y - 5, x + 5, y + 5);
			break;
		}
		}
	}

	public static void makeTreeHouse(BattleMap m, int x1, int y1, int x2, int y2) {
		m.fillArea(x1 + 1, y1, x2 - 1, y2, m.wall());
		m.fillArea(x1, y1 + 1, x2, y2 - 1, m.wall());
		m.fillArea(x1 + 2, y1 + 1, x2 - 2, y2 - 1, m.floor());
		m.fillArea(x1 + 1, y1 + 2, x2 - 1, y2 - 2, m.floor());

	}

	public static void makeTempleCircle(BattleMap m, int x, int y) {
		makeTreeBlock(m, x - 6, y - 2);
		makeTreeBlock(m, x - 5, y - 5);
		makeTreeBlock(m, x - 2, y - 6);
		makeTreeBlock(m, x + 1, y - 6);
		makeTreeBlock(m, x + 4, y - 5);
		makeTreeBlock(m, x + 5, y - 2);
		makeTreeBlock(m, x + 5, y + 1);
		makeTreeBlock(m, x + 4, y + 4);
		makeTreeBlock(m, x + 1, y + 5);
		makeTreeBlock(m, x - 2, y + 5);
		makeTreeBlock(m, x - 5, y + 4);
		makeTreeBlock(m, x - 6, y + 1);

		Thing t;

		t = Lib.create("wood priestess");
		m.addThing(t, x, y);
		AI.setGuard(t, m, x - 3, y - 3, x + 3, y + 3);

		t = Lib.create("wood priest");
		m.addThing(t, x, y);
		AI.setGuard(t, m, x - 7, y - 6, x - 1, y + 6);

		t = Lib.create("wood priest");
		m.addThing(t, x, y);
		AI.setGuard(t, m, x + 1, y - 6, x + 7, y + 6);
	}

	public static void makeTreeBlock(BattleMap m, int x, int y) {
		m.fillArea(x, y, x + 1, y + 1, Tile.TREE);
	}

	public static BattleMap makeDarkForest(int level) {
		BattleMap m = new BattleMap(71, 71);
		int w = m.getWidth();
		int h = m.getHeight();

		m.setTheme("deepforest");
		m.set("Level", level);
		m.set("WanderingRate", 300);
		m.set("Description", "Dark Forest");
		m.set("EnterMessageFirst",
				"You hear many eerie howls - it seems you are not alone in this dark forest!");

		m.fillArea(0, 0, w - 1, h - 1, m.floor());

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				switch (RPG.d(10)) {
				case 1:
					m.setTile(x, y, m.wall());
					break;
				case 2:
					m.addThing("thorny bush", x, y);
					break;
				default:
				}
			}
		}

		m.setEntrance(Lib.create("invisible portal"));
		m.addThing(m.getEntrance(), 0, 0, w - 1, 0);

		m.addThing(Lib.create("wood temple"), 0, h / 2, w - 1, h - 1);

		return m;
	}

	public static void makeDeepForest(BattleMap m) {
		int w = m.getWidth();
		int h = m.getHeight();

		m.setTheme("deepforest");
		m.set("Description", "Deep Forest");
		m.set("Level", 4);
		m.set("EnterMessageFirst", "The forest here is dark and dangerous");

		// border to left, right bottom
		m.fillArea(0, 0, w - 1, h - 1, m.wall());
		m.fillArea(1, 0, w - 2, h - 2, 0);

		// entrance centre - top
		m.setEntrance(Portal.create());
		m.setTile(w / 2, h - 1, m.floor());
		m.addThing(m.getEntrance(), w / 2, h - 1);

		// put in the entrance
		// Point es=new Point(w/4,RPG.rspread(w/4,(w*3)/4));
		// if (RPG.d(2)==1) {
		// int t=es.x; es.x=es.y; es.y=t;
		// }
		// if (RPG.d(2)==1) {
		// es.x=w-es.x; es.y=h-es.y;
		// }
		m.setEntrance(Portal.create());
		m.addThing(m.getEntrance(), w / 2, h - 1);

		Point es = new Point(w / 2, 3 * h / 4);

		makePath(m, w / 2, h - 4, es.x, es.y);
		m.fillArea(w / 2 - 1, h - 8, w / 2 + 1, h - 1, m.floor());
		m.addThing(Coin.createMoney(600), es.x, es.y);
		// addThing(new Coin(1,20),es.x,es.y);

		// your goal
		makeClearing(m, w - es.x, h - es.y, true);
		makeHut(m, w - es.x - 5, h - es.y - 4, w - es.x + 5, h - es.y + 4);

		// make a route
		Point tp = new Point(RPG.r(w - 2) + 1, RPG.r(m.getHeight() - 2) + 1);
		makePath(m, es.x, es.y, tp.x, tp.y);
		makePath(m, tp.x, tp.y, w - es.x, h - es.y);

		for (int loop = 0; loop < 10; loop++) {
			Point ls = m.findFreeSquare();
			int cx = RPG.r(w - 2) + 1;
			int cy = RPG.r(h - 2) + 1;
			makeClearing(m, cx, cy, true);
			makePath(m, ls.x, ls.y, cx, cy);
		}

		m.replaceTiles(0, m.wall());

		decorateForest(m, 0, 0, w - 1, h - 1);

		// drop in some critters
		for (int i = 0; i < 8; i++) {
			addBeasties(m, RPG.r(w), RPG.r(h));
			Point p = m.findFreeSquare();
			addBaddie(m, p.x, p.y);
		}

		/*
		 * // the infinite dungeon Point ids = m.findFreeSquare(); //Point
		 * dps=new Point(w/2,h-3); Thing id = Portal.create("infinite portal");
		 * m.addThing(id, ids.x, ids.y);
		 */
	}

	private static void makePath(BattleMap m, int x1, int y1, int x2, int y2) {
		int dx = 0;
		int dy = 0;
		if (x1 <= 0 || x2 >= m.getWidth() - 1 || y1 <= 0
				|| y2 >= m.getHeight() - 1) {
			Game.warn("DeepForest.makePath() bounds error");
		}
		while (x1 != x2 || y1 != y2) {
			if (m.getTile(x1, y1) == 0) {
				m.setTile(x1, y1, m.floor());
			}
			if (RPG.d(4) == 1) {
				dx = RPG.sign(x2 - x1);
				dy = RPG.sign(y2 - y1);
			} else {
				dx = RPG.r(3) - 1;
				dy = RPG.r(3) - 1;
			}
			if (dx != 0 && dy != 0) {
				switch (RPG.d(2)) {
				case 1:
					dx = 0;
					break;
				case 2:
					dy = 0;
					break;
				}
			}
			x1 += dx;
			y1 += dy;
			x1 = RPG.middle(1, x1, m.getWidth() - 2);
			y1 = RPG.middle(1, y1, m.getHeight() - 2);
		}
	}

	private static void makeClearing(BattleMap m, int x, int y, boolean dec) {
		int w = RPG.d(8);
		int h = RPG.d(8);
		int x1 = x - w;
		int y1 = y - h;
		int x2 = x + w;
		int y2 = y + h;

		int cx = (x1 + x2) / 2;
		int cy = (y1 + y2) / 2;

		for (int lx = x1; lx <= x1 + w * 2; lx++) {
			for (int ly = y1; ly < y1 + h * 2; ly++) {
				if ((lx - cx) * (lx - cx) * 100 / (w * w) + (ly - cy)
						* (ly - cy) * 100 / (h * h) < 100) {
					if (m.getTile(lx, ly) == 0) {
						m.setTile(lx, ly, m.floor());
					}
				}
			}
		}

		if (dec) {
			if (w >= 4 && h >= 4 && RPG.d(6) == 1 && x != m.getEntrance().x) {
				// do a fairy ring
				for (int i = 0; i < RPG.d(10); i++) {
					float a = RPG.random() * 100;
					int mx = (int) (0.5 + cx + 3 * Math.sin(a));
					int my = (int) (0.5 + cy + 3 * Math.cos(a));
					if (!m.isBlocked(mx, my)) {
						m.addThing(Lib.create("mushroom"), mx, my);
					}
				}

				for (int i = 0; i <= RPG.d(2, 4); i++) {
					Thing c = Lib.create("rabbit");
					c.set(RPG.ST_TARGETX, cx);
					c.set(RPG.ST_TARGETX, cy);
					m.addThing(c, cx, cy);
				}
			}
		}
	}

	public static void makeHut(BattleMap m, int x1, int y1, int x2, int y2) {
		m.fillArea(x1, y1, x2, y2, m.floor());
		int w = x2 - x1 - 4;
		int h = y2 - y1 - 4;

		if (h < 3 || w < 3) {
			return;
		}

		m.fillArea(x1 + 1, y1 + 1, x2 - 1, y2 - 1, Tile.CAVEWALL);
		m.fillArea(x1 + 2, y1 + 2, x2 - 2, y2 - 2, Tile.FLOOR);

		int doorx;
		int doory;

		if (RPG.d(2) == 1) {
			doorx = RPG.rspread(x1 + 2, x2 - 2);
			doory = RPG.d(2) == 1 ? y1 + 1 : y2 - 1;
		} else {
			doory = RPG.rspread(y1 + 2, y2 - 2);
			doorx = RPG.d(2) == 1 ? x1 + 1 : x2 - 1;
		}

		m.setTile(doorx, doory, m.floor());
		m.addThing(Lib.createType("IsDoor", m.getLevel()), doorx, doory);

		Thing p = Portal.create("old dungeon");
		m.addThing(p, RPG.rspread(x1 + 2, x2 - 2), RPG.rspread(y1 + 2, y2 - 2));

		m.addThing("canoe", x1, y1, x2, y2);
		m.addThing(Lib.create("goblin"), RPG.rspread(x1 + 2, x2 - 2),
				RPG.rspread(y1 + 2, y2 - 2));
		m.addThing(Lib.create("goblin"), RPG.rspread(x1 + 2, x2 - 2),
				RPG.rspread(y1 + 2, y2 - 2));
		m.addThing(Lib.createItem(0), RPG.rspread(x1 + 2, x2 - 2),
				RPG.rspread(y1 + 2, y2 - 2));
		m.addThing(Lib.createItem(0), RPG.rspread(x1 + 2, x2 - 2),
				RPG.rspread(y1 + 2, y2 - 2));
		m.addThing(Food.createFood(0), RPG.rspread(x1 + 2, x2 - 2),
				RPG.rspread(y1 + 2, y2 - 2));
		m.addThing(Food.createFood(0), RPG.rspread(x1 + 2, x2 - 2),
				RPG.rspread(y1 + 2, y2 - 2));
	}

	public static void decorateForest(BattleMap m, int x1, int y1, int x2, int y2) {
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				if (m.isBlocked(x, y)) {
					continue;
				}
				if (!(m.getTile(x, y) == m.floor())) {
					continue;
				}
				switch (RPG.d(60)) {
				case 1:
				case 2:
					m.addThing(Lib.create("bush"), x, y);
					break;
				case 5:
					m.addThing(Lib.create("thorny bush"), x, y);
					break;
				case 6:
					m.addThing(Lib.create("stone bench"), x, y);
					break;
				}
			}
		}

	}

	private static void addBeasties(BattleMap m, int x, int y) {
		Thing b;
		switch (RPG.d(5)) {
		case 1:
			b = Lib.create("dog");
			break;
		default:
			b = Lib.create("small spider");
			break;
		}

		for (int i = 0; i < 4; i++) {
			int bx = x - 2 + RPG.r(5);
			int by = y - 2 + RPG.r(5);
			if (m.isClear(bx, by)) {
				m.addThing(b, bx, by);
				b = b.cloneType(); // create more of the same
			}
		}
	}

	private static void addBaddie(BattleMap m, int x, int y) {
		if (m.isClear(x, y)) {
			Thing b = Lib.create("mutant");
			b.addThing(Lib.createItem(0));
			m.addThing(b, x, y);
		}
	}
}