package javelin.model.world.location.dungeon.feature;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.table.HiddenTrap;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.divination.FindTraps.FindingTraps;
import javelin.model.unit.skill.DisableDevice;
import javelin.model.unit.skill.Perception;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.RPG;
import javelin.view.screen.DungeonScreen;

/**
 * A trap is a semi-permanent strategic dungeon feature that deals subdual
 * (non-lethal) damage to a single character. It is balanced by generating more
 * {@link Chest}.
 *
 * Traps are currently on the average party CR -5 to -8 so as to be easy
 * encounters. It's no fun to step onto a hard trap you didn't see coming and
 * possibly lose the game. They are there more as a strategic path consideration
 * when exploring a dungeon.
 *
 * In d20 traps are supposed to award XP as well as gold. This isn't done here
 * partly because since the traps are permanent (so as to be strategic) it would
 * be easy to continually step on traps to plunder xp.
 *
 * TODO evasion upgrade (half damage/no damage instead of full/half)
 *
 * @author alex
 */
public class Trap extends Feature {
	/**
	 * TODO this is entirely circunstancial. Hopefully, at the most basic,
	 * adding new trap times should allow for lesser CRs.
	 */
	public static final int MINIMUMCR = -2;
	static final String CONFIRM = "Are you sure you want to step in to this trap?\n"
			+ "Press ENTER to confirm or any other key to quit...";

	/** Challenge rating. */
	public final int cr;
	/** Difficulty class to save against. */
	public float reflexdc;
	/** See {@link DisableDevice}. */
	public int disarmdc = -1;
	/** See {@link Perception}. */
	public int searchdc = -1;
	/** d6 */
	private int damagedie = 0;

	/**
	 * @param p
	 *            Creates a trap at this point.
	 */
	public Trap(int cr, Point p) {
		super(p.x, p.y, "dungeontrap");
		this.cr = cr;
		draw = !Dungeon.gettable(HiddenTrap.class).rollboolean();
		stop = true;
		remove = false;
		int currentcr = -1;// doesn't kill ("subdual damage", kinda)
		while (currentcr != cr || damagedie < 1) {
			reflexdc = RPG.r(10, 35);
			disarmdc = RPG.r(10, 35);
			searchdc = RPG.r(10, 35);
			damagedie = 0;
			currentcr = calculatecr();
			damagedie = Math.max(1, (cr - currentcr) * 2);
			currentcr = calculatecr();
		}
	}

	int calculatecr() {
		return ratefactor(reflexdc) + ratefactor(searchdc)
				+ ratefactor(disarmdc) + damagedie / 2;
	}

	static int ratefactor(float f) {
		if (f <= 15) {
			return -1;
		}
		if (f <= 24) {
			return 0;
		}
		if (f <= 29) {
			return 1;
		}
		return 2;
	}

	@Override
	public boolean activate() {
		if (!draw) {
			draw = true;
			spring();
			return true;
		}
		Combatant disabler = Squad.active.getbest(Skill.DISABLEDEVICE);
		int disable = disabler.taketen(Skill.DISABLEDEVICE);
		if (disable >= disarmdc) {
			Javelin.prompt(disabler + " disarms the trap!");
			Dungeon.active.features.remove(this);
			return true;
		}
		if (disable <= disarmdc - 5 || Javelin.prompt(CONFIRM).equals('\n')) {
			spring();
			return true;
		}
		DungeonScreen.dontenter = true;
		return false;
	}

	void spring() {
		String status = "You step onto a trap!\n";
		ArrayList<Combatant> targets = new ArrayList<>();
		for (Combatant c : Squad.active.members) {
			if (c.hp > 1) {
				targets.add(c);
			}
		}
		if (targets.isEmpty()) {
			targets.add(Squad.active.members.get(0));
		}
		Combatant target = RPG.pick(targets);
		if (RPG.r(1, 20) + target.source.ref >= reflexdc) {
			status += target + " dodges!";
		} else {
			int damage = -target.source.dr;
			for (int i = 0; i < damagedie; i++) {
				damage += RPG.r(1, 6);
			}
			if (damage < 0) {
				damage = 0;
			}
			target.hp = Math.max(1, target.hp - damage);
			status += target + " is " + target.getstatus() + ".";
		}
		Javelin.message(status, true);
	}

	@Override
	public void discover(Combatant searching, int searchroll) {
		super.discover(searching, searchroll);
		if (draw) {
			return;
		}
		boolean success = searchroll >= searchdc;
		if (!success && searching != null) {
			for (Combatant c : Squad.active.members) {
				int bonus = c.hascondition(FindingTraps.class) == null ? 0 : +5;
				searchroll = Math.max(searchroll,
						searching.taketen(Skill.PERCEPTION) + bonus);
			}
			success = searchroll >= searchdc;
		}
		if (success) {
			draw = true;
			generate();
		}
	}
}
