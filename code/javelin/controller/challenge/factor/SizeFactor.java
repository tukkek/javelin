package javelin.controller.challenge.factor;

import javelin.model.unit.Monster;
import javelin.model.unit.Size;

/**
 * @see CrFactor
 */
public class SizeFactor extends CrFactor{
	@Override
	public float calculate(final Monster monster){
		switch(monster.size){
			case Size.FINE:
				return 1.35f;
			case Size.DIMINUTIVE:
				return .3f;
			case Size.TINY:
				return .05f;
			case Size.SMALL:
				return 0f;
			case Size.MEDIUM:
				return 0f;
			case Size.LARGE:
				return .4f;
			case Size.HUGE:
				return .7f;
			case Size.GARGANTUAN:
				return 1f;
			case Size.COLOSSAL:
				return 1f;
			default:
				throw new RuntimeException("Invalid size. > gargantuan = 2.1");
		}
	}
}