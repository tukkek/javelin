package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#search}.
 * 
 * @author alex
 */
public class Search extends SkillUpgrade {
	public Search(String name) {
		super(name);
	}

	@Override
	protected int getranks(Skills s) {
		return s.search;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.search = ranks;
	}
}