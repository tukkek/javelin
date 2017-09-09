package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.upgrade.classes.Commoner;
import javelin.controller.upgrade.classes.Expert;
import javelin.controller.upgrade.classes.Warrior;
import javelin.model.unit.Monster;

/**
 * Feats (which are not given by class advancemente) are discounted on
 * {@link FeatsFactor}. Note that this removes CR that is more relevant on
 * {@link SkillsFactor} and {@link AbilitiesFactor}.
 *
 * @see CrFactor
 * @see ClassLevelUpgrade
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
		ClassLevelUpgrade.init();
		for (ClassLevelUpgrade c : ClassLevelUpgrade.classes) {
			final float perlevel = ABILITY + c.crperlevel
					- SkillsFactor.levelupcost(c.skillrate, monster);
			cr += c.getlevel(monster) * perlevel;
		}
		return cr;
	}

	@Override
	public void registerupgrades(UpgradeHandler handler) {
		handler.wind.add(Expert.SINGLETON);
		handler.fire.add(Warrior.SINGLETON);
		handler.water.add(Aristocrat.SINGLETON);
		handler.earth.add(Commoner.SINGLETON);
		handler.good.add(Aristocrat.SINGLETON);
		handler.evil.add(Commoner.SINGLETON);
		handler.magic.add(Aristocrat.SINGLETON);
	}
}
