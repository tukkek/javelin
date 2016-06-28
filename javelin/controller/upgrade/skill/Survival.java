package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#survival}.
 * 
 * @author alex
 */
public class Survival extends SkillUpgrade {
	public Survival() {
		super("Survival");
	}

	@Override
	protected int getranks(Skills s) {
		return s.survival;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.survival = ranks;
	}
}