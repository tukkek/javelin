package javelin.model.unit.condition;

import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/**
 * @see Monster#breaths
 * 
 * @author alex
 */
public class Breathless extends Condition {

	public Breathless(float expireatp, Combatant c) {
		super(expireatp, c, Effect.NEUTRAL, "breathless", null);
	}

	@Override
	public void start(Combatant c) {
	}

	@Override
	public void end(Combatant c) {
	}

}
