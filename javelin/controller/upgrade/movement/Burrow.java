package javelin.controller.upgrade.movement;

import javelin.model.unit.Monster;

/**
 * Upgrades burrow speed.
 * 
 * @see Monster#burrow
 */
public class Burrow extends WalkingSpeed {
	/** See {@link WalkingSpeed#WalkingSpeed(String, int).} */
	public Burrow(String name, int target) {
		super(name, target);
	}

	@Override
	protected long getSpeed(Monster m) {
		return m.burrow;
	}

	@Override
	protected void setSpeed(Monster m) {
		m.burrow = target;
	}
}
