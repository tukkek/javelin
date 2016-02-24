package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.classes.ClassAdvancement;
import javelin.model.unit.Monster;

/**
 * @see CrFactor
 */
public class ClassLevelFactor extends CrFactor {

	@Override
	public float calculate(Monster monster) {
		return monster.commoner * .3f + monster.expert * .45f
				+ monster.aristocrat * .5f + monster.warrior * .55f;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.wind.add(ClassAdvancement.EXPERT);
		handler.earth.add(ClassAdvancement.COMMONER);
		handler.water.add(ClassAdvancement.ARISTOCRAT);
		handler.fire.add(ClassAdvancement.WARRIOR);
	}
}
