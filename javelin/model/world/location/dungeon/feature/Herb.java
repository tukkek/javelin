package javelin.model.world.location.dungeon.feature;

import javelin.controller.challenge.RewardCalculator;
import javelin.model.item.Potion;
import javelin.model.unit.skill.Survival;

/**
 * Similar to a {@link Chest} but requires someone with enough {@link Survival}
 * and results in a higher-than average {@link Potion} treasure.
 *
 * @see RewardCalculator
 * @author alex
 */
public class Herb extends Feature {
	/** Constructor. */
	public Herb(int xp, int yp) {
		super(xp, yp, "dungeonherb");
	}

	@Override
	public boolean activate() {
		// TODO Auto-generated method stub
		return false;
	}
}
