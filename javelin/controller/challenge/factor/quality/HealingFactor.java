package javelin.controller.challenge.factor.quality;

import javelin.controller.challenge.factor.CrFactor;
import javelin.model.unit.Monster;

public class HealingFactor extends CrFactor {
	@Override
	public float calculate(final Monster monster) {
		return monster.fasthealing * .075f;
	}

}
