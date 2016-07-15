package javelin.controller.upgrade.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#search}.
 * 
 * @author alex
 */
public class Search extends SkillUpgrade {
	public final static SkillUpgrade SINGLETON = new Search();

	Search() {
		super("Search");
	}

	@Override
	public int getranks(Skills s) {
		return s.search;
	}

	@Override
	public void setranks(Skills s, int ranks) {
		s.search = ranks;
	}

	@Override
	protected int getattribute(Monster m) {
		return m.intelligence;
	}
}