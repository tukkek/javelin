package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#listen}.
 * 
 * @author alex
 */
public class Listen extends SkillUpgrade {
	public Listen(String name) {
		super(name);
	}

	@Override
	protected int getranks(Skills s) {
		return s.listen;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.listen = ranks;
	}
}