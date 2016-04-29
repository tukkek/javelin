package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#concentration}.
 * 
 * @author alex
 */
public class Concentration extends SkillUpgrade {
	public Concentration(String name) {
		super(name);
	}

	@Override
	protected int getranks(Skills s) {
		return s.concentration;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.concentration = ranks;
	}
}