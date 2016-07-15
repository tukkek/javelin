package javelin.controller.upgrade.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#knowledge}.
 * 
 * @author alex
 */
public class Knowledge extends SkillUpgrade {
	public final static SkillUpgrade SINGLETON = new Knowledge();

	Knowledge() {
		super("Knowledge");
	}

	@Override
	public int getranks(Skills s) {
		return s.knowledge;
	}

	@Override
	public void setranks(Skills s, int ranks) {
		s.knowledge = ranks;
	}

	@Override
	protected int getattribute(Monster m) {
		return m.intelligence;
	}
}