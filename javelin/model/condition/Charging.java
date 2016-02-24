package javelin.model.condition;

import javelin.controller.action.Charge;
import javelin.model.unit.Combatant;

/**
 * @see Charge
 * 
 * @author alex
 */
public class Charging extends Condition {

	public Charging(float expireat, Combatant c) {
		super(expireat, c, Effect.NEGATIVE, "charging");
	}

	@Override
			void start(Combatant c) {
		c.acmodifier -= 2;
	}

	@Override
			void end(Combatant c) {
		c.acmodifier += 2;
	}

}
