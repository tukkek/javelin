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