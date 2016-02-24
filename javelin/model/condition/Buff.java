package javelin.model.condition;

import javelin.model.unit.Combatant;

/**
 * Permanent buff for the battle.
 * 
 * @author alex
 */
public class Buff extends Condition {

	public Buff(String description, Combatant c) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, description);
	}

	@Override
			void start(Combatant c) {
		//
	}

	@Override
			void end(Combatant c) {
		//
	}
}
