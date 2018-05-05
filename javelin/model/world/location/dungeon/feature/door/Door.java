package javelin.model.world.location.dungeon.feature.door;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.door.trap.Alarm;
import javelin.model.world.location.dungeon.feature.door.trap.ArcaneLock;
import javelin.model.world.location.dungeon.feature.door.trap.DoorTrap;
import javelin.model.world.location.dungeon.feature.door.trap.HoldPortal;
import tyrant.mikera.engine.RPG;

public class Door extends Feature {
	/**
	 * TODO used as a workaround while we don't have a take-20 Dungeon
	 * interaction.
	 */
	private static final int BREAKBONUS = 5;
	static final List<Class<? extends Door>> TYPES = new ArrayList<Class<? extends Door>>();
	static final List<DoorTrap> TRAPS = Arrays.asList(new DoorTrap[] {
			Alarm.INSTANCE, ArcaneLock.INSTANCE, HoldPortal.INSTANCE });

	static {
		registerdoortype(WoodenDoor.class, 3);
		registerdoortype(GoodWoodenDoor.class, 2);
		registerdoortype(ExcellentWoodenDoor.class, 2);
		registerdoortype(StoneDoor.class, 1);
		registerdoortype(IronDoor.class, 1);
	}

	static void registerdoortype(Class<? extends Door> d, int chances) {
		for (int i = 0; i < chances; i++) {
			TYPES.add(d);
		}
	}

	public int unlockdc = RPG.r(20, 30);
	public int breakdc;
	/** Used if {@link #hidden}. */
	public int finddc = RPG.r(20, 30);

	boolean stuck = RPG.chancein(2); // or 5-20%?
	boolean locked = RPG.chancein(4);
	DoorTrap trap = RPG.chancein(6) ? RPG.pick(TRAPS) : null;
	/** @see #finddc */
	boolean hidden = false;

	public Door(String avatar, int breakdcstuck, int breakdclocked) {
		super(-1, -1, avatar);
		enter = false;
		stop = true;
		breakdc = locked ? breakdclocked : breakdcstuck;
		if (trap != null) {
			trap.generate(this);
		}
	}

	@Override
	public boolean activate() {
		Combatant unlocker = unlock();
		Combatant forcer = force();
		if (locked) {
			if (unlocker != null) {
				Javelin.message(unlocker + " unlocks the door!", false);
			} else if (forcer != null) {
				Javelin.message(forcer + " forces the lock!", false);
			} else {
				Javelin.message("The door's lock is too complex...", false);
				return false;
			}
		} else if (stuck) {
			if (forcer != null) {
				Javelin.message(forcer + " breaks the door open!", false);
			} else {
				Javelin.message("The door is stuck and too heavy to open...",
						false);
				return false;
			}
		}
		enter = true;
		remove();
		spring(trap, unlocker == null ? forcer : unlocker);
		return true;
	}

	void spring(DoorTrap trap, Combatant opening) {
		if (trap == null) {
			return;
		}
		if (opening == null) {
			for (Combatant c : Squad.active.members) {
				if (opening == null || c.hp > opening.hp) {
					opening = c;
				}
			}
		}
		trap.activate(opening);
	}

	Combatant unlock() {
		Combatant expert = null;
		for (Combatant c : Squad.active.members) {
			if (expert == null
					|| c.source.skills.disable(c.source) > expert.source.skills
							.disable(expert.source)) {
				expert = c;
			}
		}
		int disableroll = expert.source.skills.disable(expert.source);
		return disableroll >= unlockdc ? expert : null;
	}

	Combatant force() {
		Combatant strongest = null;
		for (Combatant c : Squad.active.members) {
			if (strongest == null
					|| c.source.strength > strongest.source.strength) {
				strongest = c;
			}
		}
		int breakroll = 10 + BREAKBONUS
				+ Monster.getbonus(strongest.source.strength);
		return breakroll >= breakdc ? strongest : null;
	}

	public static Door generate(Dungeon dungeon, Point p) {
		try {
			Door d = RPG.pick(TYPES).newInstance();
			if (!d.stuck && !d.locked && !(d.trap instanceof Alarm)) {
				return null;
			}
			d.x = p.x;
			d.y = p.y;
			d.unlockdc += dungeon.level;
			return d;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
