package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#hide}.
 * 
 * @author alex
 */
public class Hide extends SkillUpgrade {
	public Hide(String name) {
		super(name);
	}

	@Override
	protected int getranks(Skills s) {
		return s.hide;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.hide = ranks;
	}
}