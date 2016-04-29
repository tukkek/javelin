/**
 * Alex Henry on 13/10/2010
 */
package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseConsitution;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.model.unit.Monster;

/**
 * TODO leaving charisma and intelligence for now because they are useless with
 * the current game content. Probably both will be needed once skills are
 * implemented.
 * 
 * @see CrFactor
 */
public class AbilitiesFactor extends CrFactor {
	public static final float COST = .1f;

	@Override
	public float calculate(final Monster monster) {
		final int[] abilites = new int[] { monster.strength, monster.dexterity,
				monster.constitution, monster.wisdom, monster.intelligence,
				monster.charisma };
		float sum = 0;
		for (final int a : abilites) {
			if (a != 0) {
				sum += a - 10.5;
			}
		}
		sum = sum * COST;
		if (monster.intelligence == 0) {
			/**
			 * Immune to mind-affecting effects. Should be .5 but right now is
			 * only making automatic will saves.
			 */
			sum += .5;
		}
		return sum;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.earth.add(new RaiseConsitution());
		handler.fire.add(new RaiseStrength());
		handler.wind.add(new RaiseDexterity());
		handler.water.add(new RaiseWisdom());
		handler.good.add(new RaiseIntelligence());
		handler.fire.add(new RaiseCharisma());
	}
}