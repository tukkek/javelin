package javelin.model.condition.totem;

import javelin.model.condition.Condition;
import javelin.model.spell.totem.CatsGrace;
import javelin.model.unit.Combatant;

/** See {@link CatsGrace}. */
public class Graceful extends Condition {

	/**
	 * Constructor.
	 * 
	 * @param casterlevelp
	 */
	public Graceful(Combatant c, Integer casterlevelp) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, "graceful", casterlevelp);
	}

	@Override
	public void start(Combatant c) {
		c.source.raisedexterity(+2);
	}

	@Override
	public void end(Combatant c) {
		c.source.raisedexterity(-2);
	}
}
