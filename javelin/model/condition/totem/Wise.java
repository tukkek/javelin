package javelin.model.condition.totem;

import javelin.model.condition.Condition;
import javelin.model.spell.totem.OwlsWisdom;
import javelin.model.unit.Combatant;

/** See {@link OwlsWisdom}. */
public class Wise extends Condition {

	/**
	 * Constructor.
	 * 
	 * @param casterlevelp
	 */
	public Wise(Combatant c, Integer casterlevelp) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, "wise", casterlevelp);
	}

	@Override
	public void start(Combatant c) {
		c.source.raisewisdom(+4);
	}

	@Override
	public void end(Combatant c) {
		c.source.raisewisdom(+4);
	}
}
