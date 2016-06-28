package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#gatherinformation}.
 * 
 * @author alex
 */
public class GatherInformation extends SkillUpgrade {
	public GatherInformation() {
		super("Gather information");
	}

	@Override
	protected int getranks(Skills s) {
		return s.gatherinformation;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.gatherinformation = ranks;
	}
}