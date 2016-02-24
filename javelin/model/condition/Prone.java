package javelin.model.condition;

import javelin.model.feat.ImprovedTrip;
import javelin.model.unit.Combatant;

/**
 * @see ImprovedTrip
 * @author alex
 */
public class Prone extends Condition {

	public Prone(float expireatp, Combatant c) {
		super(expireatp, c, Effect.NEGATIVE, "prone");
	}

	@Override
			void start(Combatant c) {
		// -2 to AC against melee
		// +2 to AC against ranged
	}

	@Override
			void end(Combatant c) {
		// gets up (move action)
		c.ap += .5f;
	}

}
