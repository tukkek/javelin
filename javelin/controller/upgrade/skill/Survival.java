package javelin.controller.upgrade.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#survival}.
 * 
 * @author alex
 */
public class Survival extends SkillUpgrade {
	public final static SkillUpgrade SINGLETON = new Survival();

	Survival() {
		super("Survival");
	}

	@Override
	public int getranks(Skills s) {
		return s.survival;
	}

	@Override
	public void setranks(Skills s, int ranks) {
		s.survival = ranks;
	}

	@Override
	protected int getattribute(Monster m) {
		return m.wisdom;
	}
}