package javelin.model.world.location.dungeon;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.unit.Skills;
import javelin.model.unit.Squad;
import javelin.view.screen.DungeonScreen;
import tyrant.mikera.engine.RPG;

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

	transient private BattleMap map;
	/** See {@link #searchdc}. */
	public boolean found = false;
	/** Challenge rating. */
	public final int cr;
	/** Difficulty class to save against. */
	public float reflexdc;
	/** See {@link Skills#disabledevice}. */
	public int disarmdc = -1;
	/** See {@link Skills#search}. */
	public int searchdc = -1;
	/** d6 */
	private int damagedie = 0;

	/**
	 * @param p
	 *            Creates a trap at this point.
	 */
	public Trap(Point p) {
		super("poison trap", p.x, p.y, "dungeontrap");
		float sum = 0;
		for (Combatant c : Squad.active.members) {
			sum += ChallengeRatingCalculator.calculateCr(c.source);
		}
		cr = Math.round(
				Math.max(1, sum / Squad.active.members.size() - RPG.r(8, 5)));
		int currentcr = -1;// doesn't kill ("subdual damage")
		while (currentcr != cr || damagedie < 1) {
			reflexdc = RPG.r(10, 35);
			disarmdc = RPG.r(10, 35);
			searchdc = RPG.r(10, 35);
			damagedie = 0;
			currentcr = calculatecr();
			damagedie = (cr - currentcr) * 2;
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
		if (!found) {
			if (map != null) {
				found = true;
				super.addvisual(map);
				map = null;
			}
			fallinto();
			return true;
		}
		int disarm = Squad.active.disarm() - disarmdc;
		if (found && disarm >= 0) {
			Javelin.prompt("You disarm the trap!");
			super.remove();
			return true;
		}
		if (!found || /* disarm <= -5 || */ Javelin
				.prompt("Are you sure you want to step in to this trap?\n"
						+ "Press ENTER to confirm or any other key to quit...")
				.equals('\n')) {
			fallinto();
			return true;
		}
		DungeonScreen.dontmove = true;
		return true;
	}

	void fallinto() {
		String status = "You step onto a trap!\n";
		ArrayList<Combatant> targets = new ArrayList<Combatant>();
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
			status += target + " is " + target.getStatus() + ".";
		}
		Javelin.message(status, true);
	}

	@Override
	protected void addvisual(BattleMap map) {
		if (found) {
			super.addvisual(map);
		} else { // delay
			this.map = map;
		}
	}

	@Override
	public void remove() {
		// don't
	}

	/** Shows this trap to the player. */
	public void discover() {
		found = true;
		generate(map);
	}
}
