package tyrant.mikera.tyrant;

import java.util.Calendar;

import javelin.controller.Movement;
import javelin.model.BattleMap;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;



/**
 * Generates Tyrant world map
 * 
 * Also Contains code for handling: long-distance travel wilderness encounters
 * creating outdoor map locations
 */
public class WorldMap {

	protected static int[] terrains = { Tile.PLAINS, Tile.PLAINS, Tile.FORESTS,
			Tile.HILLS, Tile.MOUNTAINS, Tile.SEA, Tile.SWAMP, Tile.SEA };

	public static BattleMap createWorldMap(final int w, final int h) {
		final BattleMap m = new BattleMap(w, h);

		m.set("EnterMessage", "This fertile valley is known as North Karrain");
		m.set("Description", "North Karrain Valley");
		m.set("WanderingRate", 0);
		m.set("IsWorldMap", 1);
		m.set("Level", 1);
		m.set("VisionRange", 7);
		m.set("OnAction", new EncounterAction());

		// TODO: Increased move costs and times

		for (int x = 0; x < w; x += 4) {
			m.setTile(x, 0, Tile.MOUNTAINS);
			m.setTile(x, h - 1, Tile.MOUNTAINS);
		}
		for (int y = 0; y < h; y += 4) {
			m.setTile(0, y, Tile.MOUNTAINS);
			m.setTile(w - 1, y, Tile.SEA);
		}

		for (int y = 4; y < h - 4; y += 4) {
			for (int x = 4; x < w - 4; x += 4) {
				m.setTile(x, y, terrains[RPG.r(terrains.length)]);

			}
		}

		m.setTile(16, 8, Tile.FORESTS);
		m.setTile(16, 12, Tile.FORESTS);
		m.setTile(16, 16, Tile.PLAINS);
		m.setTile(12, 12, Tile.FORESTS);
		m.setTile(8, 12, Tile.HILLS);

		m.fractalizeBlock(0, 0, w - 1, h - 1, 4);

		// starting town + quest
		m.addThing(Portal.create("town"), 16, 16);
		m.addThing(Portal.create("ruin"), 16, 8);

		// mutable thing!
		// addThing(new Mutable("beefcake"),15,15);

		// some other towns
		m.addThing(Portal.create("town"), 0, 0, m.width - 1, m.height - 1);
		m.addThing(Portal.create("town"), 0, 0, m.width - 1, m.height - 1);

		// caves
		m.addThing(Portal.create("caves"), 0, 0, m.width - 1, m.height - 1);

		// graveyard
		m.addThing(Portal.create("graveyard"), 0, 0, m.width - 1, m.height - 1);

		// goblin grotto
		m.addThing(Portal.create("grotto"));

		// graveyard
		m.addThing(Portal.create("deep dungeon"), 0, 0, m.width - 1,
				m.height - 1);

		// special for Christmas!
		final Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.MONTH) == Calendar.DECEMBER
				&& cal.get(Calendar.DAY_OF_MONTH) == 25) {
			Game.warn("Merry Christmas!!");
			m.addThing(Portal.create("Old Nyck's hut"), 0, 0, m.width - 1,
					m.height - 1);
		}

		// goblin villae
		m.addThing(Portal.create("goblin village"), 0, 0, m.width - 1,
				m.height - 1);

		// wood temple
		Point wp = null;
		for (int i = 0; i < 1000; i++) {
			wp = m.findFreeSquare();
			if (m.getTile(wp.x, wp.y) == Tile.FORESTS) {
				break;
			}
		}
		m.addThing(Portal.create("dark forest"), wp.x, wp.y);

		// dark tower
		m.addThing(Portal.create("dark tower"));

		return m;
	}

	// enter a world map square
	// - if there is a portal to a special location then use it
	// - else create the appropraite outdoor area map
	//
	public static void exitWorldMap(final BattleMap m, final int tx, final int ty) {
		final Thing h = Game.hero();

		// record movement time
		Time.logTime(Movement.moveCost(m, h, tx, ty));

		Thing p = m.getFlaggedObject(tx, ty, "IsPortal");
		if (p != null) {
			Portal.travel(p, h);
		} else {
			// put hero into a local area map
			final BattleMap nm = WorldMap.createArea(m, tx, ty);

			// phantom Portal
			p = Portal.create();
			final Thing t = nm.getEntrance();
			Portal.setDestination(p, nm, t.x, t.y);

			// could build a portal here so we can return to exact location map
			// Don't think we really want this though
			// addThing(p,tx,ty);

			Portal.travel(p, h);
		}
	}

	private static class EncounterAction extends Script {
		private static final long serialVersionUID = -5212739439548360407L;

		@Override
		public boolean handle(final Thing t, final Event ae) {
			final Thing h = Game.hero();
			// Game.warn("EncounterAction: "+time);
			return false;
		}
	}

	// called to check for random encounters
	public static void encounter(final BattleMap map, final int x, final int y) {
		if (RPG.d(20) > 1) {
			return;
		}

		final Thing h = Game.hero();

		// don't have encounters on location squares
		final Thing[] ts = map.getPortals(x, y);
		if (ts.length > 0) {
			return;
		}

		final int tile = map.getTile(x, y);
		if (tile == Tile.SEA) {
			return;
		}

		switch (RPG.d(8)) {
		case 1: {
			Game.messageTyrant("You see somebody in the distance");
			break;
		}

		case 2: {
			final Thing c = Lib.createType("IsHostile", Game.hero().getStat(
					RPG.ST_LEVEL)
					+ RPG.r(6));
			Game.messageTyrant("You see " + c.getAName()
					+ " waiting to ambush travellers");
			Game.messageTyrant("Do you want to attack? (y/n)");

			if (Game.getOption("yn") == 'y') {
				Game.messageTyrant("");

				// create location map
				final BattleMap nm = createArea(map, x, y);

				nm.addThing(Game.hero(), 32, 32);
				nm.addThing(c, 32, 25);

				// companions
				if (RPG.d(2) == 1) {
					for (int i = RPG.d(6); i > 0; i--) {
						final int gx = 28 + RPG.r(9);
						final int gy = 18 + RPG.r(7);
						if (!nm.isBlocked(gx, gy)) {
							nm.addThing(c.cloneType(), gx, gy);
						}
					}
					if (RPG.d(2) == 1 && !nm.isBlocked(32, 20)) {
						nm.addThing(Lib.createFoe(Game.hero().getStat(
								RPG.ST_LEVEL) + 1), 32, 20);
					}
				}
			} else {
				Game.messageTyrant("You avoid the encounter");
			}
			break;
		}

		case 3: {
			final Thing c = Lib
					.createFoe(Game.hero().getStat(RPG.ST_LEVEL) + 2);
			Game.messageTyrant("You see some villagers being attacked by "
					+ c.getPluralName());
			Game.messageTyrant("Do you want to aid them? (y/n)");

			if (Game.getOption("yn") == 'y') {
				// create location map
				final BattleMap nm = createArea(map, x, y);

				nm.addThing(Game.hero(), 32, 32);

				// villages
				for (int i = RPG.d(2, 3); i > 0; i--) {
					final int gx = 29 + RPG.r(7);
					final int gy = 28 + RPG.r(7);
					if (!nm.isBlocked(gx, gy)) {
						switch (RPG.d(3)) {
						case 1:
							nm.addThing(Lib.create("farmer"), gx, gy);
							break;
						case 2:
							nm.addThing(Lib.create("village girl"), gx, gy);
							break;
						case 3:
							nm.addThing(Lib.create("townswoman"), gx, gy);
							break;
						}
					}
				}

				// critters
				for (int i = RPG.d(2, 6); i > 0; i--) {
					final int gx = 28 + RPG.r(9);
					final int gy = 21 + RPG.r(7);
					if (!nm.isBlocked(gx, gy)) {
						nm.addThing(c.cloneType(), gx, gy);
					}
				}
				// boss critter
				nm.addThing(Lib
						.createFoe(Game.hero().getStat(RPG.ST_LEVEL) + 4), 32,
						20);
			} else {
				Game.messageTyrant("You leave them to their fate");
			}
			break;
		}

		case 4: {
			Game.messageTyrant("You are slowed by bad weather conditions.");
			h.incStat("APS", -1000);
			break;
		}

		case 5: {
			final int level = h.getLevel();
			if (level <= 2) {
				break;
			}

			Game.messageTyrant("You are ambushed by a horde of fearsome monsters!");
			Game.messageTyrant("[Press space to continue]");
			Game.getOption(" ");

			final BattleMap nm = createArea(map, x, y);
			nm.set("IsHostile", 1);

			nm.addThing(Game.hero(), 32, 32);

			for (int i = 0; i < 40 + 3 * level; i++) {
				nm.addThing(Lib.createType("IsMonster", level));
			}

			break;
		}

		case 6: {
			final Thing[] its = h.getItems();
			Thing it = its.length > 0 ? its[RPG.r(its.length)] : null;
			if (it != null && !it.getFlag("IsCursed") && Item.value(it) > 0) {

				Game.messageTyrant("You encounter a band of nasty goblinoids!");
				Game.messageTyrant("They demand " + it.getYourName() + " as 'tax'");
				Game.messageTyrant("Do you want to fight them? (y/n)");
				char c = Game.getOption("yn");
				while (!(c == 'y' || c == 'n')) {
					c = Game.getOption("yn");
				}

				if (c == 'n') {
					it.remove();
					if (RPG.d(50) <= h.getLevel()) {
						Game.messageTyrant("They decide to attack you anyway!");
						c = 'y';
					} else {
						Game.messageTyrant("They take " + it.getYourName()
								+ " and run away gleefully");
					}
				} else {
					// don't lose an item
					it = null;
				}

				if (c == 'y') {
					final BattleMap nm = createArea(map, x, y);
					nm.set("IsHostile", 1);

					nm.addThing(Game.hero(), 32, 32);
					nm.addThing(it, 32, 36);
					final int level = h.getLevel();

					for (int i = 0; i < 2 + level; i++) {
						nm.addThing(Lib.createType("IsGoblinoid", level),
								h.x - 3, h.y + 1, h.x + 3, h.y + 4);
					}
				}
			}

			break;
		}

		}
	}

	// create Outdoor Area for specified square
	// can drop hero strainght into this
	private static BattleMap createArea(final BattleMap m, final int tx, final int ty) {
		final BattleMap newmap = Outdoors.create(m.getTile(tx, ty));

		// link back to worldmap
		Portal.setDestination(newmap.getEntrance(), m, tx, ty);

		return newmap;
	}

	public static BattleMap createWorld() {
		final BattleMap m = Portal.loadFromFile("/maps/world/karrain.txt");

		m.set("EnterMessage", "This fertile valley is known as North Karrain");
		m.set("OnAction", new EncounterAction());

		return m;

	}

	/*
	 * public static Map createWorld() { // create starting world Map m =
	 * createWorldMap(33, 33); Game.hero().set("WorldMap", m); Thing
	 * port=Portal.create("tutorial inn"); m.addThing(port,16,14); return m; }
	 */
}