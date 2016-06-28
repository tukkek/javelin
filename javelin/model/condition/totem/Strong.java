package javelin.model.condition.totem;

import javelin.model.condition.Condition;
import javelin.model.spell.totem.BullsStrength;
import javelin.model.unit.Combatant;

/** See {@link BullsStrength}. */
public class Strong extends Condition {

	/**
	 * Constructor.
	 * 
	 * @param casterlevelp
	 */
	public Strong(Combatant c, Integer casterlevelp) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, "strong", casterlevelp);
	}

	@Override
	public void start(Combatant c) {
		c.source.raisestrength(+2);
	}

	@Override
	public void end(Combatant c) {
		c.source.raisestrength(-2);
	}
}
