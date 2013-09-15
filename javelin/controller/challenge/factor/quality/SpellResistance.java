package javelin.controller.challenge.factor.quality;

import javelin.controller.challenge.factor.CrFactor;
import javelin.model.unit.Monster;

public class SpellResistance extends CrFactor {
	@Override
	public float calculate(Monster monster) {
		if (monster.sr == 0) {
			return 0;
		}
		if (monster.sr == Integer.MAX_VALUE) {
			return 10;
		}
		if (monster.sr <= 10) {
			throw new RuntimeException("Spell resistance too low!");
		}
		return (monster.sr - 10) * .1f;
	}

}
