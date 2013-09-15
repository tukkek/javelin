package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.classes.ClassAdvancement;
import javelin.model.unit.Monster;

public class ClassLevelFactor extends CrFactor {

	@Override
	public float calculate(Monster monster) {
		return monster.commoner * .3f + monster.expert * .45f
				+ monster.aristocrat * .5f + monster.warrior * .55f;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		for (ClassAdvancement c : ClassAdvancement.CLASSES) {
			handler.defensive.add(c);
		}
	}
}
