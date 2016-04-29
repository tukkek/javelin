package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;

/**
 * @see CrFactor
 */
public class ClassLevelFactor extends CrFactor {
	/**
	 * Normally a character gains 1 bonus ability point for every 4 levels it
	 * has so this value is to be used to counter-weight the challenge rating
	 * calculation.
	 */
	private static final float ABILITY = -AbilitiesFactor.COST / 4f;

	@Override
	public float calculate(Monster monster) {
		int level = monster.commoner + monster.expert + monster.aristocrat
				+ monster.warrior;
		return level * ABILITY + monster.commoner * .45f
				- SkillsFactor.levelup(2, monster) + monster.expert * .65f
				- SkillsFactor.levelup(6, monster) + monster.aristocrat * .65f
				- SkillsFactor.levelup(4, monster) + monster.warrior * .7f
				- SkillsFactor.levelup(2, monster);
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		// See UpgradeHandler#distribute
	}
}
