package javelin.model.unit.condition;

import javelin.controller.action.Charge;
import javelin.model.unit.attack.Combatant;

/**
 * @see Charge
 * 
 * @author alex
 */
public class Charging extends Condition {

	public Charging(float expireat, Combatant c) {
		super(expireat, c, Effect.NEGATIVE, "charging", null);
	}

	@Override
	public void start(Combatant c) {
		c.acmodifier -= 2;
	}

	@Override
	public void end(Combatant c) {
		c.acmodifier += 2;
	}

}
