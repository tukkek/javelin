package javelin.controller.upgrade.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#disabledevice}.
 * 
 * @author alex
 */
public class DisableDevice extends SkillUpgrade {
	public final static SkillUpgrade SINGLETON = new DisableDevice();

	DisableDevice() {
		super("Disable device");
	}

	@Override
	public int getranks(Skills s) {
		return s.disabledevice;
	}

	@Override
	public void setranks(Skills s, int ranks) {
		s.disabledevice = ranks;
	}

	@Override
	protected int getattribute(Monster m) {
		return m.intelligence;
	}
}