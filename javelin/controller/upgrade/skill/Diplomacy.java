package javelin.controller.upgrade.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#diplomacy}.
 * 
 * @author alex
 */
public class Diplomacy extends SkillUpgrade {
	public final static SkillUpgrade SINGLETON = new Diplomacy();

	/** Constructor. */
	Diplomacy() {
		super("Diplomacy");
	}

	@Override
	public int getranks(Skills s) {
		return s.diplomacy;
	}

	@Override
	public void setranks(Skills s, int ranks) {
		s.diplomacy = ranks;
	}

	@Override
	protected int getattribute(Monster m) {
		return m.charisma;
	}
}