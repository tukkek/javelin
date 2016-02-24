package javelin.model.condition;

import javelin.controller.action.Wait;
import javelin.model.unit.Combatant;

/**
 * @see Wait
 * 
 * @author alex
 */
public class Defending extends Condition {

	public Defending(float expireatp, Combatant c) {
		super(expireatp, c, Effect.POSITIVE, "defending");
	}

	@Override
			void start(Combatant c) {
		c.acmodifier += 4;
	}

	@Override
			void end(Combatant c) {
		c.acmodifier -= 4;
	}
}
