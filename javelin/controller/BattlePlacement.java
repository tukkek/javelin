package javelin.controller;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.exception.GaveUpException;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Combatant;
import tyrant.mikera.engine.BaseObject;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Portal;

public class BattlePlacement {
	private static final int MAXDISTANCE = 9;

	public static BattleMap place() {
		final BattleMap m = Portal.getTargetMap(Game.instance().createWorld()
				.find(Javelin.mapType));
		m.period = Javelin.DEBUGPERIOD == null ? Javelin.getDayPeriod()
				: Javelin.DEBUGPERIOD;
		clear(m);
		for (int i = 0; i < 1000; i++) {
			try {
				placecombatants(m);
				return m;
			} catch (GaveUpException e) {
				continue;
			}
		}
		throw new RuntimeException("Gave up on trying to place parties.");
	}

	public static void clear(final BattleMap m) {
		for (final Thing t : m.getThings()) {
			inheritance: for (BaseObject i = t.getInherited(); i != null; i = i
					.getInherited()) {
				for (final String s : new String[] { "trap", "ladder",
						"temple", "portal", "secret", "being", "monster",
						"door" }) {
					if (i.toString().contains(s)) {
						m.removeThing(t);
						break inheritance;
					}
				}
			}
		}
	}

	public static void placecombatants(final BattleMap m)
			throws GaveUpException {
		List<Combatant> queuea = RPG.r(1, 2) == 1 ? BattleMap.blueTeam
				: BattleMap.redTeam;
		List<Combatant> queueb = queuea == BattleMap.blueTeam ? BattleMap.redTeam
				: BattleMap.blueTeam;
		queuea = new ArrayList<Combatant>(queuea);
		queueb = new ArrayList<Combatant>(queueb);
		final Combatant seeda = RPG.pick(queuea);
		final Combatant seedb = RPG.pick(queueb);
		final BattleState s = m.getState();
		add(m, seeda, randompoint(s, 0, m.height - 1, 0, m.width - 1), s);
		place(seedb, seeda, m, s);
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
			place(c, RPG.pick(placed), m, s);
			markplaced(c, queue, placed);
		}
	}

	public static void markplaced(final Combatant seeda,
			final List<Combatant> queue, final ArrayList<Combatant> placed) {
		queue.remove(seeda);
		placed.add(seeda);
	}

	public static void place(final Combatant placing,
			final Combatant reference, final BattleMap m, final BattleState s)
			throws GaveUpException {
		int vision = placing.view(s.period);
		if (vision > 8 || vision > MAXDISTANCE) {
			vision = MAXDISTANCE;
		}
		final ArrayList<java.awt.Point> possibilities = mappossibilities(
				reference, vision, s);
		while (!possibilities.isEmpty()) {
			java.awt.Point p = RPG.pick(possibilities);
			placing.location[0] = p.x;
			placing.location[1] = p.y;
			Vision path = s.hasLineOfSight(placing, reference);
			if (path == Vision.CLEAR || placing.source.fly > 0
					&& path == Vision.BLOCKED) {
				add(m, placing, p, s);
				break;
			}
			possibilities.remove(p);
		}
		if (possibilities.isEmpty()) {
			throw new GaveUpException();
		}
	}

	public static ArrayList<java.awt.Point> mappossibilities(
			final Combatant reference, int vision, final BattleState s) {
		final ArrayList<java.awt.Point> possibilities = new ArrayList<java.awt.Point>();
		for (int x = reference.location[0] - vision; x <= reference.location[0]
				+ vision; x++) {
			if (isbound(x, s.map)) {
				for (int y = reference.location[1] - vision; y <= reference.location[1]
						+ vision; y++) {
					if (isbound(y, s.map[0]) && !s.map[x][y].blocked
							&& s.getCombatant(x, y) == null) {
						possibilities.add(new java.awt.Point(x, y));
					}
				}
			}
		}
		return possibilities;
	}

	public static boolean isbound(final int y, final Object[] squares) {
		return 0 <= y && y < squares.length - 1;
	}

	public static void add(final BattleMap m, final Combatant c,
			final java.awt.Point p, BattleState s) {
		c.visual = addThing(m, p, "dog");
		c.visual.combatant = c;
		(BattleMap.blueTeam.contains(c) ? s.blueTeam : s.redTeam).add(c);
		c.location[0] = p.x;
		c.location[1] = p.y;
	}

	static private Thing addThing(final BattleMap tm, final java.awt.Point p,
			final String name) {
		final Thing t = Lib
				.create(((Thing) Lib.get(name)).getString("Name"), 1);
		t.place = tm;
		t.x = p.x;
		t.y = p.y;
		tm.addThing(t, p.x, p.y);
		return t;
	}

	static public java.awt.Point randompoint(final BattleState s, int minx,
			int maxx, int miny, int maxy) {
		while (true) {
			final int x = RPG.r(minx, maxx);
			final int y = RPG.r(miny, maxy);
			if (!s.map[x][y].blocked && s.getCombatant(x, y) == null) {
				return new java.awt.Point(x, y);
			}
		}
	}
}
