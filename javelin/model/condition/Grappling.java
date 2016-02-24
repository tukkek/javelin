package javelin.model.condition;

import javelin.model.feat.ImprovedGrapple;
import javelin.model.unit.Combatant;

/**
 * @see ImprovedGrapple
 * @author alex
 */
public class Grappling extends Condition {

	public Grappling(float expireatp, Combatant c) {
		super(expireatp, c, Effect.NEGATIVE, "grappling");
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
