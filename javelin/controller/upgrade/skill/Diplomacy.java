package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#diplomacy}.
 * 
 * @author alex
 */
public class Diplomacy extends SkillUpgrade {
	/** Constructor. */
	public Diplomacy() {
		super("Diplomacy");
	}

	@Override
	protected int getranks(Skills s) {
		return s.diplomacy;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.diplomacy = ranks;
	}
}