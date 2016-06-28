package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#acrobatics}.
 * 
 * @author alex
 */
public class Acrobatics extends SkillUpgrade {
	public Acrobatics() {
		super("Acrobatics");
	}

	@Override
	protected int getranks(Skills s) {
		return s.acrobatics;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.acrobatics = ranks;
	}
}