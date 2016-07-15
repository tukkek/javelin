package javelin.controller.upgrade.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#stealth}. Unified Pathfinder version of Move Silently
 * and Hide from the original d20 rules.
 * 
 * @see Perception
 * @author alex
 */
public class Stealth extends SkillUpgrade {
	public final static SkillUpgrade SINGLETON = new Stealth();

	Stealth() {
		super("Stealth");
	}

	@Override
	public int getranks(Skills s) {
		return s.stealth;
	}

	@Override
	public void setranks(Skills s, int ranks) {
		s.stealth = ranks;
	}

	@Override
	protected int getattribute(Monster m) {
		return m.dexterity;
	}
}