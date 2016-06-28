package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#perception}. This was unified by Pathfinder from the
 * original Spot and Listen d20 skills.
 * 
 * @see Stealth
 * @author alex
 */
public class Perception extends SkillUpgrade {
	/** Constructor. */
	public Perception() {
		super("Perception");
	}

	@Override
	protected int getranks(Skills s) {
		return s.perception;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.perception = ranks;
	}
}