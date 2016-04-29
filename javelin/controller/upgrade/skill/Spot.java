package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#spot}.
 * 
 * @author alex
 */
public class Spot extends SkillUpgrade {
	public Spot(String name) {
		super(name);
	}

	@Override
	protected int getranks(Skills s) {
		return s.spot;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.spot = ranks;
	}
}