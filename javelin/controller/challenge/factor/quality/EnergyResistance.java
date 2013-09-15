package javelin.controller.challenge.factor.quality;

import javelin.controller.challenge.factor.CrFactor;
import javelin.model.unit.Monster;

/**
 * They should be .02CR but currently the game assume 1 point of energy
 * resistance is actually 5 (one for each type of energy resistance).
 * 
 * @author alex
 * @see javelin.controller.quality.EnergyResistance#RESISTANCETYPES
 * @see Monster#resistance
 */
public class EnergyResistance extends CrFactor {
	@Override
	public float calculate(Monster monster) {
		if (monster.resistance == Integer.MAX_VALUE) {
			return 5;
		}
		return monster.resistance * .02f * 5;
	}

}
