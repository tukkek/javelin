package javelin.controller.upgrade.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#concentration}.
 * 
 * @author alex
 */
public class Concentration extends SkillUpgrade {
	public final static SkillUpgrade SINGLETON = new Concentration();

	Concentration() {
		super("Concentration", true);
	}

	@Override
	public int getranks(Skills s) {
		return s.concentration;
	}

	@Override
	public void setranks(Skills s, int ranks) {
		s.concentration = ranks;
	}

	@Override
	protected int getattribute(Monster m) {
		return m.constitution;
	}
}