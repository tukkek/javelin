package javelin.controller.upgrade.skill;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * See {@link Skills#usemagicdevice}
 * 
 * @author alex
 */
public class UseMagicDevice extends SkillUpgrade {

	/** Constructor. */
	public UseMagicDevice() {
		super("Use magic device");
	}

	@Override
	protected int getranks(Skills s) {
		return s.usemagicdevice;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.usemagicdevice = ranks;
	}

	@Override
	protected boolean apply(Combatant c) {
		if (maximize(c) + Monster.getbonus(c.source.charisma) < 10) {
			/* Since minimum DC is 20 (wand) less than 10 bonus is useless. */
			return false;
		}
		return super.apply(c);
	}
}
