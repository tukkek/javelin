package javelin.controller.upgrade.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.Skills;
import javelin.model.unit.attack.Combatant;

/**
 * See {@link Skills#usemagicdevice}
 * 
 * @author alex
 */
public class UseMagicDevice extends SkillUpgrade {
	public final static SkillUpgrade SINGLETON = new UseMagicDevice();

	/** Constructor. */
	UseMagicDevice() {
		super("Use magic device", true);
	}

	@Override
	public int getranks(Skills s) {
		return s.usemagicdevice;
	}

	@Override
	public void setranks(Skills s, int ranks) {
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

	@Override
	protected int getattribute(Monster m) {
		return m.charisma;
	}
}
