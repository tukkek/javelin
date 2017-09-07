package javelin.model.unit.condition.totem;

import javelin.model.unit.abilities.spell.totem.OwlsWisdom;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;

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
