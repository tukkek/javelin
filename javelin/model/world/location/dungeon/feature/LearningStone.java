package javelin.model.world.location.dungeon.feature;

import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Allows a {@link Combatant} to learn a single {@link Upgrade}. The upgrade is
 * applied immediately, even if results in a negative XP pool for the unit -
 * mostly because it's no fun to find a cool feature and not be able to use it.
 *
 * In the same spirit, the upgrade in question is not predetermined but chosen
 * randomly so it will at least be able to be applied to one {@link Squad}
 * member. If the upgrade isn't wanted, they can still take the stone for its
 * value in gold (scaled to the current Dungeon#level).
 *
 * @author alex
 */
public class LearningStone extends Feature {
	/** {@link Difficulty#MODERATE} value in gold. */
	int value = RewardCalculator.getgold(Math.max(1, Dungeon.active.level - 4));

	/** Constructor. */
	public LearningStone(int xp, int yp) {
		super(xp, yp, "dungeonlearningstone");
	}

	@Override
	public boolean activate() {
		return false;
	}
}
