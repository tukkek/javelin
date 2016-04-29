package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#movesilently}.
 * 
 * @author alex
 */
public class MoveSilently extends SkillUpgrade {
	public MoveSilently(String name) {
		super(name);
	}

	@Override
	protected int getranks(Skills s) {
		return s.movesilently;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.movesilently = ranks;
	}
}