/**
 *
 */
package javelin.controller.challenge.factor;

import javelin.model.unit.Monster;
import javelin.model.unit.Size;

/**
 * @see CrFactor
 */
public class SizeFactorWithAttModifiers extends CrFactor{
	@Override
	public float calculate(final Monster monster){
		switch(monster.size){
			case Size.FINE:
				return .55f;
			case Size.DIMINUTIVE:
				return -.3f;
			case Size.TINY:
				return -.55f;
			case Size.SMALL:
				return -0.4f;
			case Size.MEDIUM:
				return 0f;
			case Size.LARGE:
				return 1.4f;
			case Size.HUGE:
				return 2.9f;
			case Size.GARGANTUAN:
				return 4.4f;
			case Size.COLOSSAL:
				return 5.6f;
			default:
				throw new RuntimeException("Invalid size. > gargantuan = 2.1");
		}
	}
}