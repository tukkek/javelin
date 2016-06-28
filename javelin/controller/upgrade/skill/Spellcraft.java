package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrade {@link Skills#spellcraft}.
 * 
 * @author alex
 */
public class Spellcraft extends SkillUpgrade {
	public Spellcraft() {
		super("Spellcraft");
	}

	@Override
	protected int getranks(Skills s) {
		return s.spellcraft;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.spellcraft = ranks;
	}
}