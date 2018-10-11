package javelin.controller.challenge.factor;

import javelin.model.unit.Monster;

/**
 * @see CrFactor
 */
public class SizeFactor extends CrFactor{
	@Override
	public float calculate(final Monster monster){
		switch(monster.size){
			case Monster.FINE:
				return 1.35f;
			case Monster.DIMINUTIVE:
				return .3f;
			case Monster.TINY:
				return .05f;
			case Monster.SMALL:
				return 0f;
			case Monster.MEDIUM:
				return 0f;
			case Monster.LARGE:
				return .4f;
			case Monster.HUGE:
				return .7f;
			case Monster.GARGANTUAN:
				return 1f;
			case Monster.COLOSSAL:
				return 1f;
			default:
				throw new RuntimeException("Invalid size. > gargantuan = 2.1");
		}
	}
}