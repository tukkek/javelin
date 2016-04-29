package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#disabledevice}.
 * 
 * @author alex
 */
public class DisableDevice extends SkillUpgrade {
	public DisableDevice(String name) {
		super(name);
	}

	@Override
	protected int getranks(Skills s) {
		return s.disabledevice;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.disabledevice = ranks;
	}
}