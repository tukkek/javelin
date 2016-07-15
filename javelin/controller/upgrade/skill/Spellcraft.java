package javelin.controller.upgrade.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * Upgrade {@link Skills#spellcraft}.
 * 
 * @author alex
 */
public class Spellcraft extends SkillUpgrade {
	public final static SkillUpgrade SINGLETON = new Spellcraft();

	Spellcraft() {
		super("Spellcraft");
	}

	@Override
	public int getranks(Skills s) {
		return s.spellcraft;
	}

	@Override
	public void setranks(Skills s, int ranks) {
		s.spellcraft = ranks;
	}

	@Override
	protected int getattribute(Monster m) {
		return m.intelligence;
	}
}