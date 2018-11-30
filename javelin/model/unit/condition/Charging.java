package javelin.model.unit.condition;

import javelin.controller.action.Charge;
import javelin.model.unit.Combatant;

/**
 * @see Charge
 *
 * @author alex
 */
public class Charging extends Condition{

	public Charging(float expireat,Combatant c){
		super(c,"charging",Effect.NEGATIVE,null,expireat);
	}

	@Override
	public void start(Combatant c){
		c.acmodifier-=2;
	}

	@Override
	public void end(Combatant c){
		c.acmodifier+=2;
	}

}
