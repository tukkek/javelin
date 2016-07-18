package javelin.controller;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.db.Preferences;
import javelin.controller.exception.GaveUpException;
import javelin.controller.fight.Fight;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.map.Map;
import javelin.controller.terrain.map.MapGenerator;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * Given a {@link Map}, a {@link Squad} and a {@link Fight} setups an initial
 * battle state.
 * 
 * @author alex
 */
public class BattleSetup {
	private static final int MAXDISTANCE = 9;

	public static BattleMap place() {
		rollinitiative();
		Fight f = Javelin.app.fight;
		if (f.map == null) {
			f.map = MapGenerator.generatebattlemap(
					f.terrain == null ? Terrain.current() : f.terrain,
					Dungeon.active != null);
		}
		f.map.generate();
		BattleMap m = f.map.battlemap;
		m.period = Preferences.DEBUGPERIOD == null ? f.period
				: Preferences.DEBUGPERIOD;
		for (int i = 0; i < 1000; i++) {
			try {
				placecombatants(m);
				Weather.flood(m);
				return m;
			} catch (GaveUpException e) {
				ArrayList<Combatant> all =
						new ArrayList<Combatant>(BattleMap.blueTeam);
				all.addAll(BattleMap.redTeam);
				for (Combatant c : all) {
					if (c.visual != null) {
						c.visual.remove();
					}
				}
				continue;
			}
		}
		throw new RuntimeException("Gave up on trying to place parties.");
	}

	public static void rollinitiative() {
		List<Combatant> everyone = (List<Combatant>) BattleMap.blueTeam.clone();
		everyone.addAll(BattleMap.redTeam);
		for (final Combatant c : everyone) {
			c.rollinitiative();
			c.lastrefresh = -Float.MAX_VALUE;
		}
	}

	public static void placecombatants(final BattleMap m)
			throws GaveUpException {
		List<Combatant> queuea =
				RPG.r(1, 2) == 1 ? BattleMap.blueTeam : BattleMap.redTeam;
		List<Combatant> queueb = queuea == BattleMap.blueTeam
				? BattleMap.redTeam : BattleMap.blueTeam;
		queuea = new ArrayList<Combatant>(queuea);
		queueb = new ArrayList<Combatant>(queueb);
		final Combatant seeda = RPG.pick(queuea);
		final Combatant seedb = RPG.pick(queueb);
		final BattleState s = m.getState();
		add(m, seeda, randompoint(s, 0, m.height - 1, 0, m.width - 1), s);
		placecombatant(seedb, seeda, m, s);
		final ArrayList<Combatant> placeda = new ArrayList<Combatant>();
		final ArrayList<Combatant> placedb = new ArrayList<Combatant>();
		markplaced(seeda, queuea, placeda);
		markplaced(seedb, queueb, placedb);
		while (!queuea.isEmpty() || !queueb.isEmpty()) {
			placeteammate(queuea, placeda, m, s);
			placeteammate(queueb, placedb, m, s);
		}
		BattleMap.combatants = s.getCombatants();
		m.setState(s);
	}

	public static void placeteammate(final List<Combatant> queue,
			final ArrayList<Combatant> placed, final BattleMap m,
			final BattleState s) throws GaveUpException {
		if (!queue.isEmpty()) {
			Combatant c = RPG.pick(queue);
			placecombatant(c, RPG.pick(placed), m, s);
			markplaced(c, queue, placed);
		}
	}

	public static void markplaced(final Combatant seeda,
			final List<Combatant> queue, final ArrayList<Combatant> placed) {
		queue.remove(seeda);
		placed.add(seeda);
	}

	public static void placecombatant(final Combatant placing,
			final Combatant reference, final BattleMap m, final BattleState s)
			throws GaveUpException {
		int vision = placing.view(s.period);
		if (vision > 8 || vision > MAXDISTANCE) {
			vision = MAXDISTANCE;
		}
		final ArrayList<Point> possibilities =
				mappossibilities(reference, vision, s);
		while (!possibilities.isEmpty()) {
			Point p = RPG.pick(possibilities);
			placing.location[0] = p.x;
			placing.location[1] = p.y;
			Vision path = s.hasLineOfSight(placing, reference);
			if (path == Vision.CLEAR) {
				add(m, placing, p, s);
				break;
			}
			possibilities.remove(p);
		}
		if (possibilities.isEmpty()) {
			throw new GaveUpException();
		}
	}

	public static ArrayList<Point> mappossibilities(final Combatant reference,
			int vision, final BattleState s) {
		final ArrayList<Point> possibilities = new ArrayList<Point>();
		for (int x = reference.location[0] - vision; x <= reference.location[0]
				+ vision; x++) {
			if (isbound(x, s.map)) {
				for (int y = reference.location[1]
						- vision; y <= reference.location[1] + vision; y++) {
					if (isbound(y, s.map[0]) && !s.map[x][y].blocked
							&& s.getCombatant(x, y) == null) {
						possibilities.add(new Point(x, y));
					}
				}
			}
		}
		return possibilities;
	}

	public static boolean isbound(final int y, final Object[] squares) {
		return 0 < y && y < squares.length - 2;
	}

	public static void add(final BattleMap m, final Combatant c, final Point p,
			BattleState s) {
		c.visual = addThing(m, p, "dog");
		c.visual.combatant = c;
		(BattleMap.blueTeam.contains(c) ? s.blueTeam : s.redTeam).add(c);
		c.location[0] = p.x;
		c.location[1] = p.y;
	}

	public static Thing addThing(final BattleMap tm, final Point p,
			final String name) {
		final Thing t =
				Lib.create(((Thing) Lib.get(name)).getString("Name"), 1);
		t.place = tm;
		t.x = p.x;
		t.y = p.y;
		tm.addThing(t, p.x, p.y);
		return t;
	}

	static public Point randompoint(final BattleState s, int minx, int maxx,
			int miny, int maxy) {
		while (true) {
			final int x = RPG.r(minx, maxx);
			final int y = RPG.r(miny, maxy);
			if (!s.map[x][y].blocked && s.getCombatant(x, y) == null) {
				return new Point(x, y);
			}
		}
	}
}
