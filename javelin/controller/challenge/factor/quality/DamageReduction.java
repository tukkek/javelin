package javelin.controller.challenge.factor.quality;

import javelin.controller.challenge.factor.CrFactor;
import javelin.model.unit.Monster;

public class DamageReduction extends CrFactor {

	@Override
	public float calculate(Monster monster) {
		return monster.dr * .2f;
	}

}
