/**
 * 
 */
package javelin.controller.challenge.factor;

import javelin.model.unit.Monster;

/**
 * @see CrFactor
 */
public class SizeFactorWithAttModifiers extends CrFactor {
	@Override
	public float calculate(final Monster monster) {
		switch (monster.size) {
		case Monster.FINE:
			return .55f;
		case Monster.DIMINUTIVE:
			return -.3f;
		case Monster.TINY:
			return -.55f;
		case Monster.SMALL:
			return -0.4f;
		case Monster.MEDIUM:
			return 0f;
		case Monster.LARGE:
			return 1.4f;
		case Monster.HUGE:
			return 2.9f;
		case Monster.GARGANTUAN:
			return 4.4f;
		case Monster.COLOSSAL:
			return 5.6f;
		default:
			throw new RuntimeException("Invalid size. > gargantuan = 2.1");
		}
	}
}