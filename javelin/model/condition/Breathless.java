package javelin.model.condition;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see Monster#breaths
 * 
 * @author alex
 */
public class Breathless extends Condition {

	public Breathless(float expireatp, Combatant c) {
		super(expireatp, c, Effect.NEUTRAL, "breathless");
	}

	@Override
			void start(Combatant c) {
	}

	@Override
			void end(Combatant c) {
	}

}
