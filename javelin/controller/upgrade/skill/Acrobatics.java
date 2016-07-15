package javelin.controller.upgrade.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#acrobatics}.
 * 
 * @author alex
 */
public class Acrobatics extends SkillUpgrade {
	public final static SkillUpgrade SINGLETON = new Acrobatics();

	Acrobatics() {
		super("Acrobatics");
	}

	@Override
	public int getranks(Skills s) {
		return s.acrobatics;
	}

	@Override
	public void setranks(Skills s, int ranks) {
		s.acrobatics = ranks;
	}

	@Override
	protected int getattribute(Monster m) {
		return m.dexterity;
	}
}