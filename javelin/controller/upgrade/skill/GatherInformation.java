package javelin.controller.upgrade.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#gatherinformation}.
 * 
 * @author alex
 */
public class GatherInformation extends SkillUpgrade {
	public final static SkillUpgrade SINGLETON = new GatherInformation();

	GatherInformation() {
		super("Gather information");
	}

	@Override
	public int getranks(Skills s) {
		return s.gatherinformation;
	}

	@Override
	public void setranks(Skills s, int ranks) {
		s.gatherinformation = ranks;
	}

	@Override
	protected int getattribute(Monster m) {
		return m.charisma;
	}
}