package javelin.model.condition.totem;

import javelin.model.condition.Condition;
import javelin.model.spell.totem.FoxsCunning;
import javelin.model.unit.Combatant;

/** See {@link FoxsCunning}. */
public class Cunning extends Condition {

	/**
	 * Constructor.
	 * 
	 * @param casterlevelp
	 */
	public Cunning(Combatant c, Integer casterlevelp) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, "cunning", casterlevelp);
	}

	@Override
	public void start(Combatant c) {
		c.source.intelligence += 4;
	}

	@Override
	public void end(Combatant c) {
		c.source.intelligence += 4;
	}
}
