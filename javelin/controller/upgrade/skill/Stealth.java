package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#stealth}. Unified Pathfinder version of Move Silently
 * and Hide from the original d20 rules.
 * 
 * @see Perception
 * @author alex
 */
public class Stealth extends SkillUpgrade {
	public Stealth() {
		super("Stealth");
	}

	@Override
	protected int getranks(Skills s) {
		return s.stealth;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.stealth = ranks;
	}
}