package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.classes.ClassAdvancement;
import javelin.model.unit.Monster;

/**
 * Feats (which are not given by class advancemente) are discounted on
 * {@link FeatsFactor}. Note that this removes CR that is more relevant on
 * {@link SkillsFactor} and {@link AbilitiesFactor}.
 * 
 * @see CrFactor
 * @see ClassAdvancement
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
		float cr = 0;
		for (ClassAdvancement c : ClassAdvancement.CLASSES) {
			cr += c.getlevel(monster) * (ABILITY + c.crperlevel
					- SkillsFactor.levelupcost(c.skillrate, monster));
		}
		return cr;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		// See UpgradeHandler#distribute
	}
}
