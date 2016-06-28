package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#knowledge}.
 * 
 * @author alex
 */
public class Knowledge extends SkillUpgrade {
	public Knowledge() {
		super("Knowledge");
	}

	@Override
	protected int getranks(Skills s) {
		return s.knowledge;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.knowledge = ranks;
	}
}