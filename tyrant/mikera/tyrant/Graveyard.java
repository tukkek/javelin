/*
 * Created on 12-Aug-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 * 
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class Graveyard {
	public static void makeGraveyard(BattleMap m) {
		Outdoors.buildOutdoors(m, 0, 0, m.width - 1, m.height - 1, Tile.FORESTS);

		int x1 = m.getWidth() / 4;
		int y1 = m.getHeight() / 4;
		int x2 = x1 * 3;
		int y2 = y1 * 3;

		// create an exit to catacombs
		Thing exit = Portal.create("stairs down");
		exit.set("ComplexName", "old catacombs");
		exit.set("OnTravel", new Script() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean handle(Thing t, Event e) {
				BattleMap m = t.getMap();
				m.set("IsHostile", 1);
				return false;
			}
		});
		m.setExit(exit);

		makeGraveyard(m, x1, y1, x2, y2);

		// place exit if not already done
		if (exit.getMap() == null) {
			m.addThing(exit, x1, y1, x2, y2);
		}

		m.setEntrance(Lib.create("invisible portal"));
		m.addThing(m.getEntrance(), 0, m.height - 1, m.width - 1, m.height - 1);
		m.set("IsHostile", 0);
		m.set("Level", 7);
		m.set("WanderingRate", 10);
		m.set("WanderingType", "IsUndead");
		m.set("Description", "Old Graveyard");

	}

	public static void makeGraveyard(BattleMap m, int x1, int y1, int x2, int y2) {
		m.clearArea(x1, y1, x2, y2);
		m.fillArea(x1, y1, x2, y2, Tile.GRASS);
		m.fillArea((x1 + x2) / 2, y1, (x1 + x2) / 2, y2, Tile.STONEFLOOR);
		m.fillBorder(x1, y1, x2, y2, Tile.CAVEWALL);

		m.setTile((x1 + x2) / 2, y2, m.floor());

		m.addThing(Lib.create("graveyard door"), (x1 + x2) / 2, y2);

		buildArea(m, x1 + 1, y1 + 1, (x1 + x2) / 2 - 1, y2 - 1, true);
		buildArea(m, (x1 + x2) / 2 + 1, y1 + 1, x2 - 1, y2 - 1, true);
	}

	public static void buildPatch(BattleMap m, int x1, int y1, int x2, int y2) {
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				switch (RPG.d(36)) {
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
					m.addThing("[IsGravestone]", x, y);
					break;
				case 6:
					if (RPG.d(2) == 1) {
						m.addThing("potted flower", x, y);
					} else {
						m.addThing("bird bath", x, y);
					}
					break;
				case 7:
					m.addThing("stone bench", x, y);
					break;
				case 8:
					m.addThing("menhir", x, y);
					break;
				case 9:
					m.addThing("small bush", x, y);
					break;
				case 10:
					m.addThing("tree", x, y);
					break;
				case 11:
				case 12:
				case 13:

					m.addThing("[IsGravestone]", x, y);
					break;
				}
			}
		}
	}

	public static void buildSpecialArea(BattleMap m, int x1, int y1, int x2,
			int y2) {
		switch (RPG.d(5)) {
		case 1:
			m.fillArea(x1, y1, x2, y2, Tile.POOL);
			break;
		case 2:
			m.fillArea(x1 + 1, y1 + 1, x2 - 1, y2 - 1, Tile.POOL);
			break;
		case 3:
			m.fillArea(x1 + 1, y1 + 1, x2 - 1, y2 - 1, Tile.POOL);
			m.fillArea(x1 + 2, y1 + 2, x2 - 2, y2 - 2, Tile.STONEFLOOR);
			m.fillArea(x1, (y1 + y2) / 2, x2, (y1 + y2) / 2, Tile.STONEFLOOR);
			if (x2 - x1 >= 8 && y2 - y1 >= 8) {
				buildTomb(m, x1 + 3, y1 + 3, x2 - 3, y2 - 3);
			}
			break;
		case 4:
		case 5:
			buildTomb(m, x1 + 1, y1 + 1, x2 - 1, y2 - 1);
			break;
		}

	}

	public static void buildTomb(BattleMap m, int x1, int y1, int x2, int y2) {
		m.fillArea(x1, y1, x2, y2, Tile.STONEFLOOR);
		m.fillBorder(x1, y1, x2, y2, Tile.CAVEWALL);
		m.setTile((x1 + x2) / 2, y2, Tile.STONEFLOOR);
		m.addThing("graveyard door", (x1 + x2) / 2, y2);
		decorateTomb(m, x1 + 1, y1 + 1, x2 - 1, y2 - 1);
	}

	public static void decorateTomb(BattleMap m, int x1, int y1, int x2, int y2) {
		int w = x2 - x1 + 1;
		int h = y2 - y1 + 1;

		if (w >= 3 && h >= 3) {
			m.addThing(m.getExit(), x1, y1, x2, y2);
		} else {
			for (int x = x1; x <= x2; x++) {
				for (int y = y1; y <= y2; y++) {
					if (RPG.d(2) == 1) {
						m.addThing("[IsGravestone]", x, y);
					}
				}
			}
		}
	}

	public static void buildArea(BattleMap m, int x1, int y1, int x2, int y2,
			boolean vertical) {
		int w = x2 - x1 + 1;
		int h = y2 - y1 + 1;

		if (w <= 5 || h <= 5) {
			buildPatch(m, x1, y1, x2, y2);

		} else if (RPG.d(6) == 1) {
			buildSpecialArea(m, x1, y1, x2, y2);
		} else if (vertical) {
			int s = RPG.rspread(y1 + 1, y2 - 1);
			m.fillArea(x1, s, x2, s, Tile.STONEFLOOR);
			buildArea(m, x1, y1, x2, s - 1, !vertical);
			buildArea(m, x1, s + 1, x2, y2, !vertical);

		} else {
			int s = RPG.rspread(x1 + 1, x2 - 1);
			m.fillArea(s, y1, s, y2, Tile.STONEFLOOR);
			buildArea(m, x1, y1, s - 1, y2, !vertical);
			buildArea(m, s + 1, y1, x2, y2, !vertical);

		}

	}
}
