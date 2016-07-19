/*
 * Created on 12-Jun-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package javelin.controller;

import javelin.controller.action.Dig;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.old.Game;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import tyrant.mikera.engine.Describer;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Armour;
import tyrant.mikera.tyrant.Event;
import tyrant.mikera.tyrant.Item;
import tyrant.mikera.tyrant.Tile;
import tyrant.mikera.tyrant.Weapon;

/**
 * Player movement, taken from Mike's Tyrant.
 * 
 * @author Mike
 * 
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class Movement {
	/**
	 * try to move
	 * 
	 * @param state
	 * 
	 * @return <code>true</code> if some action taken
	 */
	public static boolean tryMove(final Thing thing, final BattleMap map,
			int tx, int ty, BattleState state) {
		map.visitPath(thing.x, thing.y);
		int dx = thing.getStatIfAbsent("RunDirectionX", Integer.MIN_VALUE);
		int dy = thing.getStatIfAbsent("RunDirectionY", Integer.MIN_VALUE);
		if (dx == Integer.MIN_VALUE) {
			dx = RPG.sign(tx - thing.x);
			dy = RPG.sign(ty - thing.y);
		} else {
			tx = thing.x + dx;
			ty = thing.y + dy;
		}
		if (thing.getFlag("IsMobile")) {
			thing.set("DirectionX", dx);
			thing.set("DirectionY", dy);
		}
		if (dx == 0 && dy == 0) {
			throw new RepeatTurn();
		}
		Meld m = BattleMap.checkformeld(tx, ty);
		if (m != null && !m.crystalize(state)) {
			throw new RepeatTurn();
		}
		final Thing mob = map.getMobile(tx, ty);
		if (mob == null) {
			if (thing.getFlag("IsIntelligent") && tryBump(thing, map, tx, ty)) {
				throw new RepeatTurn();
			}
		} else {
			Combatant c = thing.combatant;
			if (!c.isAlly(mob.combatant, state)) {
				if (c.burrowed) {
					Dig.refuse();
				}
				javelin.controller.action.Movement.lastmovewasattack = true;
				c.meleeAttacks(mob.combatant, state);
				return true;
			}
			throw new RepeatTurn();
		}
		final Thing hero = Game.hero();
		if (thing == hero
				|| map.getFlaggedObject(tx, ty, "IsWarning") == null) {
			if (canMove(thing, map, tx, ty)) {
				return acceptMove(thing, map, tx, ty);
			}
		}
		throw new RepeatTurn();
	}

	private static boolean acceptMove(final Thing thing, final BattleMap map,
			final int tx, final int ty) {
		// thing.combatant.move();
		return doMove(thing, map, tx, ty);
	}

	private static final double rt2 = Math.sqrt(2);

	// public static boolean tryDig(final Thing b, final BattleMap m, final int
	// x,
	// final int y) {
	// if (Tile.isDiggable(m, x, y) && Tile.dig(b, m, x, y)) {
	// return true;
	// }
	// return false;
	// }

	// public static int calcMoveSpeed(final Thing t) {
	// int ms = t.getStat(RPG.ST_MOVESPEED);
	// final int ath = t.getStat(Skill.ATHLETICS);
	// if (ath > 0) {
	// ms += RPG.min(100, t.getStat("AG")) * ath / 4;
	// }
	// return ms;
	// }

	// public static int moveCost(final BattleMap m, final Thing t, final int
	// tx,
	// final int ty) {
	// double mcost = m.getMoveCost(tx, ty);
	//
	// if (t.getFlag("IsFlying")) {
	// mcost = 100;
	// }
	//
	// if (m.getFlag("IsWorldMap")) {
	// mcost *= 20;
	// }
	//
	// final int d = (tx - t.x) * (tx - t.x) + (ty - t.y) * (ty - t.y);
	// if (d == 2) {
	// mcost = mcost * rt2;
	// }
	//
	// // base movement cost plus encumberance
	// int basecost = t.getStat(RPG.ST_MOVECOST);
	// basecost += t.getStat(RPG.ST_ENCUMBERANCE);
	//
	// mcost = mcost * basecost / calcMoveSpeed(t);
	//
	// if (mcost == 0) {
	// Game.warn(t.getName(Game.hero()) + " has zero move cost!");
	// }
	// return (int) mcost;
	// }

	// public static boolean push(final Thing t, final int dx, final int dy) {
	// final BattleMap map = t.getMap();
	// if (map == null || t.place != map) {
	// return false;
	// }
	// final int tx = t.x + dx;
	// final int ty = t.y + dy;
	// if (!map.isBlocked(tx, ty)) {
	// t.moveTo(map, tx, ty);
	// return true;
	// }
	// return false;
	// }

	// public static void jump(final Thing h, final int x, final int y) {
	// final BattleMap m = h.getMap();
	//
	// double sx = h.x;
	// double sy = h.y;
	// double dx = x - sx;
	// double dy = y - sy;
	// if (dx == 0.0 && dy == 0.0) {
	// return;
	// }
	// int d = 0;
	//
	// if (Math.abs(dx) < Math.abs(dy)) {
	// d = (int) Math.abs(y - sy);
	// dx = dx / Math.abs(dy);
	// dy = dy > 0 ? 1 : -1;
	// } else {
	// d = (int) Math.abs(x - sx);
	// dy = dy / Math.abs(dx);
	// dx = dx > 0 ? 1 : -1;
	// }
	//
	// final int maxJump = (int) (1.0 + Math.sqrt(h.getStat(Skill.ATHLETICS)));
	//
	// d = RPG.min(d, maxJump);
	//
	// for (int i = 0; i < d; i++) {
	// sx += dx;
	// sy += dy;
	// final int tx = (int) Math.round(sx);
	// final int ty = (int) Math.round(sy);
	//
	// if (canJump(h, m, tx, ty)) {
	// h.incStat("APS", -(moveCost(m, h, tx, ty) * 2));
	// flyTo(h, m, tx, ty);
	// } else {
	// h.displace();
	// return;
	// }
	// }
	//
	// }

	private static boolean tryBump(final Thing being, final BattleMap m,
			final int tx, final int ty) {
		Thing head = m.getObjects(tx, ty);
		while (head != null) {
			if (head.isBlocking() && head.handles("OnBump")) {

				final Event e = new Event("Bump");
				e.set("Target", being);
				head.handle(e);

				return e.getFlag("ActionTaken");
			}
			head = head.next;
		}
		return false;
	}

	/**
	 * Work out whether a move is phyically possible
	 * 
	 * @param t
	 *            A thing
	 * @param m
	 *            Destination map
	 * @param tx
	 *            Destination x-position on map
	 * @param ty
	 *            Destination y-position on map
	 * @return True if move is possible
	 */
	public static boolean canMove(final Thing t, final BattleMap m,
			final int tx, final int ty) {
		if (t.combatant != null && t.combatant.source.fly > 0) {
			return true;
		}
		if (m.isObjectBlocked(tx, ty)) {
			return false;
		}
		return canMoveToTile(t, m, tx, ty);
	}

	private static boolean canMoveToTile(final Thing t, final BattleMap m,
			final int tx, final int ty) {
		return Tile.isPassable(t, m, tx, ty);
	}

	// public static boolean canJump(final Thing t, final BattleMap m,
	// final int tx, final int ty) {
	// if (canMove(t, m, tx, ty)) {
	// return true;
	// }
	//
	// if (!m.isBlocked(tx, ty)) {
	// return true;
	// }
	//
	// final int type = m.getTile(tx, ty);
	//
	// if (Tile.isFilling(type)) {
	// return false;
	// }
	//
	// final Thing[] ts = m.getThings(tx, ty);
	// for (final Thing tt : ts) {
	// if (tt.getFlag("IsBlocking") && !tt.getFlag("IsJumpable")) {
	// return false;
	// }
	// }
	//
	// return true;
	// }

	/**
	 * Teleport a Thing to a new map location
	 * 
	 * @param t
	 *            A Thing
	 * @param m
	 *            The destination Map
	 * @param tx
	 *            Destination x-coordinate
	 * @param ty
	 *            Destination y-coordinate
	 */
	public static void teleport(final Thing t, final BattleMap m, final int tx,
			final int ty) {
		moveTo(t, m, tx, ty);
	}

	/**
	 * Move a Thing to a new map location
	 * 
	 * @param t
	 *            A Thing
	 * @param m
	 *            The destination Map
	 * @param tx
	 *            Destination x-coordinate
	 * @param ty
	 *            Destination y-coordinate
	 */
	public static void moveTo(Thing t, final BattleMap m, final int tx,
			final int ty) {
		t = m.addThing(t, tx, ty);
		if (t.place != m) {
			throw new Error("Thing not added!");
		}
	}

	// /**
	// * Move a Thing to a new map location
	// *
	// * @param t
	// * A Thing
	// * @param m
	// * The destination Map
	// * @param tx
	// * Destination x-coordinate
	// * @param ty
	// * Destination y-coordinate
	// */
	// public static void flyTo(final Thing t, final BattleMap m, final int tx,
	// final int ty) {
	// m.addThing(t, tx, ty);
	// Movement.enterTrigger(t, m, tx, ty, false);
	// if (t.isHero()) {
	// locationMessage();
	// }
	// }

	/**
	 * Process special events that occur
	 * 
	 * @param t
	 *            A Thing
	 * @param m
	 *            The map where the thing has been placed
	 * @param tx
	 *            Map x-coordinate
	 * @param ty
	 *            Map y-coordinate
	 * @param touchFloor
	 *            Whether the Thing touches the floor
	 */
	private static void enterTrigger(final Thing t, final BattleMap m,
			final int tx, final int ty, final boolean touchFloor) {
		final Thing[] things = m.getThings(tx, ty);
		boolean handled = false;
		for (final Thing tt : things) {
			if (tt.place == m && tt.handles("OnEnterTrigger")) {
				final Event te = new Event("EnterTrigger");
				te.set("Target", t);
				te.set("TouchFloor", touchFloor);
				handled |= tt.handle(te);
			}
		}

		if (!handled) {
			Tile.enterTrigger(t, m, tx, ty, touchFloor);
		}
	}

	/**
	 * Display messages when hero moves to a new location
	 * 
	 */
	private static void locationMessage() {
		final Thing h = Game.hero();
		Thing t = h.getMap().getObjects(h.x, h.y);
		while (t != null) {
			if (t.getFlag("IsItem")) {
				Item.tryIdentify(h, t);
				// Give more informaton when walking over an identified item
				String s = t.getAName();
				if (t.getStat("HPS") < t.getStat("HPSMAX")) {
					s = "damaged " + s;
				}
				if (t.getFlag("IsWeapon") && Item.isIdentified(t)) {
					s = s + "  " + Weapon.statString(t);
				}
				if (t.getFlag("IsArmour") && Item.isIdentified(t)) {
					s = s + "  " + Armour.statString(t);
				}

				Game.messageTyrant(
						"There " + (Describer.isPlural(t) ? "are " : "is ") + s
								+ " here");
				h.isRunning(false);
			}
			t = t.next;
		}
	}

	private static boolean doMove(final Thing thing, final BattleMap map,
			final int tx, final int ty) {
		map.visitPath(tx, ty);
		moveTo(thing, map, tx, ty);
		// thing.incStat("APS", -moveCost(map, thing, tx, ty));
		return true;
	}

}