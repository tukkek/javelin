package javelin.model.unit.condition.totem;

import javelin.model.unit.abilities.spell.totem.FoxsCunning;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;

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
