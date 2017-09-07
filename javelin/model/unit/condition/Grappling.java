package javelin.model.unit.condition;

import javelin.model.unit.attack.Combatant;
import javelin.model.unit.feat.attack.expertise.ImprovedGrapple;

/**
 * @see ImprovedGrapple
 * @author alex
 */
public class Grappling extends Condition {

	public Grappling(float expireatp, Combatant c) {
		super(expireatp, c, Effect.NEGATIVE, "grappling", null);
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
