package javelin.controller.challenge.factor;

import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.unit.Monster;

/**
 * Feats (which are not given by class advancemente) are discounted on
 * {@link FeatsFactor}. Note that this removes CR that is more relevant on
 * {@link SkillsFactor} and {@link AbilitiesFactor}.
 *
 * @see CrFactor
 * @see ClassLevelUpgrade
 */
public class ClassLevelFactor extends CrFactor{
	/**
	 * Normally a character gains 1 bonus ability point for every 4 levels it has
	 * so this value is to be used to counter-weight the challenge rating
	 * calculation.
	 */
	private static final float ABILITY=-AbilitiesFactor.COST/4f;

	@Override
	public float calculate(Monster monster){
		float cr=0;
		ClassLevelUpgrade.setup();
		for(ClassLevelUpgrade c:ClassLevelUpgrade.classes){
			final float perlevel=ABILITY+c.crperlevel
					-SkillsFactor.levelupcost(c.skillrate,monster);
			cr+=c.getlevel(monster)*perlevel;
		}
		return cr;
	}
}
