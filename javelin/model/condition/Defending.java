package javelin.model.condition;

import javelin.controller.action.Defend;
import javelin.model.unit.Combatant;

/**
 * @see Defend
 * 
 * @author alex
 */
public class Defending extends Condition {

	public Defending(float expireatp, Combatant c) {
		super(expireatp, c, Effect.POSITIVE, "defending", null);
	}

	@Override
	public void start(Combatant c) {
		c.acmodifier += 4;
	}

	@Override
	public void end(Combatant c) {
		c.acmodifier -= 4;
	}
}
