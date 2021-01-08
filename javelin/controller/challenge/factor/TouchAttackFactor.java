package javelin.controller.challenge.factor;

import javelin.model.unit.Monster;
import javelin.model.unit.abilities.TouchAttack;

/**
 * @see TouchAttack
 * @see javelin.controller.content.action.TouchAttack
 * @see Monster#touch
 * @author alex
 */
public class TouchAttackFactor extends CrFactor{
	@Override
	public float calculate(Monster monster){
		if(monster.touch==null) return 0;
		return .03f*monster.touch.damage[0]*monster.touch.damage[1]/2f;
	}
}
