package javelin.model.unit.condition;

import javelin.model.unit.attack.Combatant;
import javelin.model.unit.feat.attack.expertise.ImprovedTrip;

/**
 * @see ImprovedTrip
 * @author alex
 */
public class Prone extends Condition {

	public Prone(float expireatp, Combatant c) {
		super(expireatp, c, Effect.NEGATIVE, "prone", null);
	}

	@Override
	public void start(Combatant c) {
		// -2 to AC against melee
		// +2 to AC against ranged
	}

	@Override
	public void end(Combatant c) {
		// gets up (move action)
		c.ap += .5f;
	}

}
