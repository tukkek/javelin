package javelin.model.condition.totem;

import javelin.model.condition.Condition;
import javelin.model.spell.totem.BearsEndurance;
import javelin.model.unit.Combatant;

/** See {@link BearsEndurance}. */
public class Enduring extends Condition {

	/**
	 * Constructor.
	 * 
	 * @param casterlevelp
	 */
	public Enduring(Combatant c, Integer casterlevelp) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, "enduring", casterlevelp);
	}

	@Override
	public void start(Combatant c) {
		c.source.raiseconstitution(c, 2);
	}

	@Override
	public void end(Combatant c) {
		c.source.raiseconstitution(c, -2);
	}
}
