package javelin.controller.quality.perception;

import javelin.model.unit.Monster;

/**
 * Comprehends low-light vision and blindsight.
 * 
 * @author alex
 */
public class LowLightVision extends Vision {
	/** See {@link Vision#Vision(String, int)}. */
	public LowLightVision(String name, int target) {
		super(name, target);
	}

	@Override
	public boolean apply(String attack, Monster m) {
		return super.apply(attack, m) || attack.contains("blindsight");
	}
}
