package tyrant.mikera.tyrant;

import java.io.Serializable;

import javelin.controller.Movement;
import javelin.model.BattleMap;

import tyrant.mikera.engine.Description;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;


/**
 * AI implements the behaviour of monsters and other beings
 * 
 * AI behaviour can be tuned through the properties of these beings.
 * 
 * @author Mike
 */
public class AI implements Serializable {
	private static final long serialVersionUID = 4051043086123415608L;
	// ai state constants
	// Used with RPG.ST_STATE characteristic
	public static final String STATE_HOSTILE = "Attack";
	public static final String STATE_FOLLOWER = "Follow";
	public static final String STATE_INHABITANT = "Wander";

	// notify event constats

	// ATTACKED event
	// ext = side attacked
	// o = attacker (usually Game.actor)
	public static final int EVENT_ATTACKED = 1;
	public static final int EVENT_VISIBLE = 2;
	public static final int EVENT_MORALECHECK = 3;
	public static final int EVENT_FEARCHECK = 4;
	public static final int EVENT_ALARM = 5;
	public static final int EVENT_THEFT = 6;

	public static void setNeutral(final Thing m) {
		m.set("IsHostile", 0);
		m.set("IsNeutral", 1);
		m.set("IsInhabitant", 1);
	}

	public static boolean isHostile(final Thing m, final Thing b) {
		// never hostile to self!
		if (b == m) {
			return false;
		}

		final boolean mhostile = m.getFlag("IsHostile");
		final boolean bhostile = b.getFlag("IsHostile");
		if (mhostile && bhostile) {
			return false;
		}

		if (m.isHero() && bhostile) {
			return true;
		}
		if (b.isHero()) {
			if (mhostile) {
				return true;
			}
			if (b.getStat("CH") < 0) {
				return true;
			}
		}

		if (m.getFlag("IsInsane") || b.getFlag("IsInsane")) {
			return true;
		}

		if (m.name().equals(b.name())) {
			return false;
		}

		if (m.getFlag("IsInhabitant") && b.getFlag("IsInhabitant")) {
			return false;
		}

		if (m.getFlag("IsNeutral") || b.getFlag("IsNeutral")) {
			return false;
		}

		if (mhostile && !bhostile) {
			return true;
		}
		if (bhostile && !mhostile) {
			return true;
		}

		return false;
	}

	public static void turnNasty(final Thing m) {
		// Could turn townies into nasties
		// but prefer changing entire map state
		// m.setAI(NastyCritterAI.instance);
		if (m.isHero()) {
			throw new Error("AI.turnNasty(): Trying to affect hero!!!");
		}
		m.set("IsHostile", 1);
		if (m.getFlag("IsInhabitant")) {
			m.getMap().setAngry(true);
		}
	}

	public static void reTarget(final Thing m, final Thing f) {
		m.set("CurrentFoe", f);
	}

	public static int notifyAttack(final Thing m, final Thing attacker) {
		notify(m, AI.EVENT_ATTACKED, 0, attacker);
		return 0;
	}

	public static int notify(final Thing m, final int eventtype, final int ext,
			final Object o) {
		final Thing other = (Thing) o;
		final BattleMap map = m.getMap();
		if (map == null) {
			return 0;
		}
		// hero doesn't respond!
		if (m.isHero()) {
			return 0;
		}
		if (isHostile(m, Game.hero())) {
			return 0;
		}

		switch (eventtype) {
		case EVENT_ATTACKED:
			if (other.isHero() && ext == m.getStat(RPG.ST_SIDE)) {
				if (m.getFlag("IsIntelligent")) {
					Game.messageTyrant(m.getTheName() + " shouts angrily!");
				}
				turnNasty(m);
			}
			return 0;
		case EVENT_VISIBLE:
			if (m.isHostile(Game.hero())) {
				reTarget(m, Game.hero());
				m.getMap().areaNotify(m.x, m.y, 5, AI.EVENT_ALARM, 0, null);
			}
			return 1;

		case EVENT_THEFT:
			if (m.isVisible(Game.hero()) && m.getFlag("IsIntelligent")) {
				if (RPG.test(m.getStat(RPG.ST_IN), other.getStat(RPG.ST_SK))) {
					Game.messageTyrant(m.getTheName() + " shouts angrily!");
					turnNasty(m);
					map.areaNotify(m.x, m.y, 10, eventtype, 1, o);
				} else {
					Game.messageTyrant(m.getTheName() + " doesn't notice");
				}
			}
			return 0;
		}
		return 0;
	}

	/**
	 * Finds an appropriate foe for a monster to attack
	 * 
	 * @param m
	 */
	public static Thing findFoe(final Thing m) {
		Thing foe = m.getThing("CurrentFoe");
		if (foe != null) {
			if (foe.place == null) {
				// previous foe has died
				foe = null;
			} else {
				// use foe if we can see it
				if (!m.canSee(foe)) {
					foe = null;
				}
			}
		}
		if (foe == null || RPG.d(12) == 1) {
			final BattleMap map = m.getMap();
			if (map == null) {
				foe = null;
			} else {
				foe = map.findNearestFoe(m);
			}
			if (foe != null) {
				m.set("CurrentFoe", foe);
			}
		}

		return foe;
	}

	public static boolean tryMove(final Thing m, final int nx, final int ny) {
		final BattleMap map = m.getMap();
		if (!Tile.isSensibleMove(m, map, nx, ny)) {
			return false;
		}
		return Movement.tryMove(m, map, nx, ny);
	}

	// creature attacks towards target tx,ty
	public static void doAttack(final Thing m, final int tx, final int ty) {
		final BattleMap map = m.getMap();
		if (map == null) {
			return;
		}
		if (RPG.d(100) <= m.getStat(RPG.ST_CASTCHANCE)) {
			if (doCasterAction(m)) {
				return;
			}
		}

		/*
		 * 
		 * if (visible && (RPG.d(3) == 1) && (m.getWielded(RPG.WT_MISSILE) !=
		 * null)) { AI.doCritterAction(m); return; }
		 */

		int dx = RPG.sign(tx - m.x);
		int dy = RPG.sign(ty - m.y);

		// run away?
		if (RPG.r(100) < m.getStat("RetreatChance") || Being.feelsFear(m)
				&& m.getStat(RPG.ST_HPS) * 3 <= m.getStat(RPG.ST_HPSMAX)) {
			if (tryMove(m, m.x - dx, m.y - dy)) {
				return;
			}
			if (tryMove(m, m.x - RPG.sign(dx - dy), m.y - RPG.sign(dy + dx))) {
				return;
			}
			if (tryMove(m, m.x - RPG.sign(dx + dy), m.y - RPG.sign(dy - dx))) {
				return;
			}
		}

		if (tryMove(m, m.x + dx, m.y + dy)) {
			return;
		}
		if (tryMove(m, m.x + RPG.sign(dx - dy), m.y + RPG.sign(dy + dx))) {
			return;
		}
		if (tryMove(m, m.x + RPG.sign(dx + dy), m.y + RPG.sign(dy - dx))) {
			return;
		}

		if (RPG.d(2) == 1) {
			dx = RPG.r(3) - 1;
		}
		if (RPG.d(2) == 1) {
			dy = RPG.r(3) - 1;
		}

		if (!tryMove(m, m.x + dx, m.y + dy)) {
			// blocked
			m.incStat("APS", -30);
			m.set("DirectionX", 0);
			m.set("DirectionY", 0);
		}
	}

	public static class AIScript extends Script {
		private static final long serialVersionUID = 3257562910623609394L;

		@Override
		public boolean handle(final Thing m, final Event e) {
			final int time = e.getStat("Time");

			// bail out if no time to perform action
			if (time <= 0) {
				return false;
			}

			int aps = m.getStat("APS") + time * m.getStat("Speed") / 100;
			m.set("APS", aps);

			if (m.getFlag("IsBeing") && aps > 0) {
				// max of 10 actions
				for (int i = 0; i < 10; i++) {
					doAction(m);
					aps = m.getStat("APS");
					if (aps <= 0) {
						break;
					}
				}
			}

			if (aps > 0) {
				// discard unused APS
				aps = 0;
				// Game.warn("APS discard ("+aps+"): "+m.name());
			}
			m.set("APS", aps);

			Being.recover(m, time);

			return false;
		}
	}

	public static boolean doAction(final Thing m) {
		m.getClass();
		return true;
		// Game.actor = m;
		//
		// String state = m.getString("AIMode");
		// final Map map = m.getMap();
		// if (map == null || !(m.place instanceof Map)) {
		// m.set("APS", 0);
		// return true;
		// }
		// final boolean canSeeHero = m.canSee(Game.hero());
		//
		// if (!m.getFlag("IsHostile") && m.getFlag("IsInhabitant")
		// && map.getFlag("IsHostile")) {
		// m.set("IsHostile", 1);
		// }
		//
		// if (state == null) {
		// state = "Wander";
		// m.set("AIMode", state);
		// }
		//
		// // do interesting actions if visible
		// if (canSeeHero) {
		// // try spell caster action
		// if (m.getFlag(Skill.CASTING) && doCasterAction(m)) {
		// return true;
		// }
		// }
		//
		// if (state.equals("Evade")) {
		// // Game.warn("Critter action...");
		// return doCritterAction(m);
		// } else if (state.equals("Guard")) {
		// // Game.warn("Guarding...");
		// return doGuardAction(m);
		// } else if (state.equals("Attack")) {
		// final Thing f = findFoe(m);
		//
		// if (f == null) {
		// m.set("AIMode", "Wander");
		// return true;
		// }
		//
		// // try ranged attack
		// if (RPG.d(3) == 1 && doRangedAttack(m, f)) {
		// return true;
		// }
		//
		// // TODO: some way of alerting unaware allies?
		// // map.areaNotify(m.x, m.y, 6, AI.EVENT_ALARM, 0, null);
		// doAttack(m, f.x, f.y);
		// return true;
		// } else if (state.equals("Wander")) {
		// final Thing f = findFoe(m);
		//
		// if (f != null) {
		// // attack the foe
		// if (RPG.d(2) == 1) {
		// m.set(RPG.ST_AIMODE, "Attack");
		// }
		// doAttack(m, f.x, f.y);
		// } else if (RPG.d(4) == 1 && m.getFlag("IsIntelligent")) {
		// // pick up and use stuff
		// doPickup(m);
		// } else {
		// int dx = m.getStat("DirectionX");
		// int dy = m.getStat("DirectionY");
		// if (dx == 0 && dy == 0 || RPG.d(2) == 1) {
		// if (RPG.d(3) == 1) {
		// dx = RPG.r(3) - 1;
		// }
		// if (RPG.d(3) == 1) {
		// dy = RPG.r(3) - 1;
		// }
		// }
		// // make a random move
		// doAttack(m, m.x + dx, m.y + dy);
		// }
		// return true;
		// } else if (state.equals("Follow")) {
		// return doFollowAction(m);
		// }
		// return false;
	}

	// turn creature into a follower
	public static void setFollower(final Thing t, final Thing leader) {
		t.set("Leader", leader);
		if (leader.isHero()) {
			t.set("AIMode", AI.STATE_FOLLOWER);
		}
		t.set("IsHostile", leader.getStat("IsHostile"));
		t.set("IsNeutral", leader.getStat("IsNeutral"));
		t.set(RPG.ST_SIDE, leader.getStat(RPG.ST_SIDE));
	}

	public static boolean isFollower(final Thing t, final Thing leader) {
		return t.get("Leader") == leader;
	}

	// private static boolean doFollowAction(final Thing m) {
	//
	// final Thing l = m.getThing("Leader");
	// if (l == null || l.isDead()) {
	// m.set("AIMode", "Wander");
	// m.set("APS", 0);
	// return true;
	// }
	//
	// final int ld = RPG.abs(m.x - l.x) + RPG.abs(m.y - l.y);
	//
	// final Thing f = findFoe(l);
	// if (f != null) {
	// final int fd = RPG.abs(f.x - m.x) + RPG.abs(f.y - m.y);
	// if (fd <= 2 || ld <= fd) {
	// doAttack(m, f.x, f.y);
	// return true;
	// }
	// }
	// if (f != null || RPG.d(6) < ld) {
	// doAttack(m, l.x, l.y);
	// return true;
	// }
	//
	// return doRandomWalk(m);
	// }

	private static boolean doRandomWalk(final Thing m) {
		// wander randomly
		final int dx = RPG.r(3) - 1;
		final int dy = RPG.r(3) - 1;
		final int nx = m.x + dx;
		final int ny = m.y + dy;
		if (!tryMove(m, nx, ny)) {
			m.incStat("APS", -100);
		}

		// why not pick up some items?
		if (RPG.d(10) == 1 && m.getStat(RPG.ST_CR) >= 5) {
			doPickup(m);
		}
		return true;
	}

	private static void doPickup(final Thing m) {
		final BattleMap map = m.getMap();
		final Thing[] stuff = map.getThings(m.x, m.y);
		boolean found = false;
		for (final Thing st : stuff) {
			if (st.getFlag("IsItem") && !Item.isOwned(st)) {
				Item.pickup(m, st);
				found = true;
			}
		}
		if (found) {
			Being.utiliseItems(m);
		}
	}

	public static boolean doCritterAction(final Thing m) {
		final Thing h = Game.hero();
		final BattleMap map = m.getMap();

		/*
		 * // sniff for food - this is a slow algorithm // so only do
		 * occasionally or if visible if (m.isVisible() || (RPG.d(40) == 1)) {
		 * int dis = 4; //search distance Thing[] search = map.getThings(m.x -
		 * dis, m.y - dis, m.x + dis, m.y + dis); for (int i = 0; i <
		 * search.length; i++) { Thing t = search[i]; if (t.getFlag("IsFood")) {
		 * m.set(RPG.ST_TARGETX, t.x); m.set(RPG.ST_TARGETY, t.y); } } //
		 * m.incStat("APS",-50); }
		 */

		final Thing f = findFoe(m);
		if (f != null && f.getStat("HPS") < m.getStat("HPS")) {
			doAttack(m, f.x, f.y);
		}

		final int d = RPG.distSquared(h.x, h.y, m.x, m.y);

		int dx;
		int dy;

		dx = RPG.r(3) - 1;
		dy = RPG.r(3) - 1;

		if (RPG.d(10) > d) {
			dx = -RPG.sign(h.x - m.x);
		}
		if (RPG.d(10) > d) {
			dy = -RPG.sign(h.y - m.y);
		}

		final int nx = m.x + dx;
		final int ny = m.y + dy;

		return Movement.tryMove(m, map, nx, ny);
	}

	public static boolean doRangedAttack(final Thing m, final Thing f) {
		final Thing rw = m.getWielded(RPG.WT_RANGEDWEAPON);
		final Thing ms = m.getWielded(RPG.WT_MISSILE);

		// Game.warn("thinking! "+rw+" and "+ms);
		if (rw != null && RangedWeapon.isValidAmmo(rw, ms)) {
			// Game.warn("shooting!");
			RangedWeapon.fireAt(rw, m, ms.remove(1), f.getMap(), f.x, f.y);
			Being.utiliseItems(m);
			return true;
		} else if (ms != null) {
			// throw a missile
			if (Combat.DEBUG) {
				Game.warn(m.name() + " throwing " + ms.name());
			}
			Missile.throwAt(ms.remove(1), m, f.getMap(), f.x, f.y);
			Being.utiliseItems(m);
			return true;
		} else {
			return false;
		}
	}

	public static boolean doCasterAction(final Thing m) {
		final Thing[] ar = m.getFlaggedContents("IsSpell");
		final int c = ar.length;
		// m.incStat("APS", - 10); // pause to think

		if (c <= 0) {
			return false;
		}

		if (m.isVisible(Game.hero()) || RPG.d(10) == 1) {
			final int i = RPG.r(c);
			final Thing a = ar[i];
			return Spell.castAI(m, a);
		}
		return false;
	}

	public static void setGuard(final Thing t, final BattleMap m, final int x1,
			final int y1) {
		setGuard(t, m, x1 - 1, y1 - 1, x1 + 1, y1 + 1);
	}

	public static void setGuard(final Thing t, final Thing gp) {
		t.set("GuardPoint", gp);
		t.set("AIMode", "Guard");
		gp.set("Guard", t);
	}

	public static void setGuard(final Thing t, final BattleMap m, final int x1,
			final int y1, final int x2, final int y2) {
		Thing gp = t.getThing("GuardPoint");
		if (gp == null) {
			gp = Lib.create("guard point");
			t.set("GuardPoint", gp);
		}
		gp.set("Guard", t);
		gp.set("GuardRadius", RPG.min(Math.abs(x2 - x1), Math.abs(y2 - y1)) / 2);
		m.addThing(gp, (x1 + x2) / 2, (y1 + y2) / 2);
		t.set("AIMode", "Guard");
	}

	public static void name(final Thing t, final String name) {
		t.set("Name", name);
		t.set("NameType", Description.NAMETYPE_PROPER);
		t.set("IsNamed", 1);
		t.set("IsUnique", 1);
		t.set("Frequency", 0);
	}

	public static boolean doGuardAction(final Thing m) {
		final BattleMap map = m.getMap();
		if (map == null) {
			return false;
		}

		Thing gp = m.getThing("GuardPoint");
		if (gp == null) {
			gp = map.getNamedObject(m.x, m.y, "guard point");

			if (gp == null) {
				Game.warn(m.name() + " has no guard point!");
				return false;
			}
			m.set("GuardPoint", gp);

		}
		final int gr = gp.getStat("GuardRadius");

		final int x1 = gp.x - gr;
		final int y1 = gp.y - gr;
		final int x2 = gp.x + gr;
		final int y2 = gp.y + gr;

		final int tx = gp.x;
		final int ty = gp.y;

		final Thing f = findFoe(m);
		if (f != null && RPG.distSquared(m.x, m.y, f.x, f.y) <= 50) {
			doAttack(m, f.x, f.y);
		} else if (m.x <= x1 || m.x >= x2 || m.y <= y1 || m.y >= y2) {
			// outside area, need to get back in!
			doAttack(m, tx, ty);
		} else {
			final boolean changedirection = RPG.d(6) == 1
					|| m.getStat("DirectionX") == 0
					&& m.getStat("DirectionY") == 0;
			if (changedirection) {
				m.set("DirectionX", RPG.r(3) - 1);
			}
			if (changedirection) {
				m.set("DirectionY", RPG.r(3) - 1);
			}
			doAttack(m, m.x + m.getStat("DirectionX"),
					m.y + m.getStat("DirectionY"));
		}
		return true;
	}

	public static boolean doFriendlyAction(final Thing m) {
		final int tx = m.getStat(RPG.ST_TARGETX);
		final int ty = m.getStat(RPG.ST_TARGETY);

		final BattleMap map = m.getMap();

		if (map.isAngry() && m.getFlag("IsIntelligent")) {
			m.set("IsHostile", 1);
		}

		int dx = RPG.r(3) - 1;
		int dy = RPG.r(3) - 1;

		if (tx > 0 && ty > 0) {
			if (RPG.d(2) == 1) {
				dx = RPG.sign(tx - m.x);
			}
			if (RPG.d(2) == 1) {
				dy = RPG.sign(ty - m.y);
			}
		}

		final int nx = m.x + dx;
		final int ny = m.y + dy;

		if (!map.isBlocked(nx, ny)) {
			m.moveTo(map, nx, ny);
		} else {
			// don't do anything. we are nice!!!
		}
		m.incStat("APS", -10000 / m.getStat(RPG.ST_MOVESPEED));
		return true;
	}

	public static void init() {
		// nothing here yet

	}
}