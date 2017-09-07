package javelin.model.unit.condition.totem;

import javelin.model.unit.abilities.spell.totem.EaglesSplendor;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;

/** See {@link EaglesSplendor}. */
public class Splendid extends Condition {

	/**
	 * Constructor.
	 * 
	 * @param casterlevelp
	 */
	public Splendid(Combatant c, Integer casterlevelp) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, "splendid", casterlevelp);
	}

	@Override
	public void start(Combatant c) {
		c.source.charisma += 4;
	}

	@Override
	public void end(Combatant c) {
		c.source.charisma -= 4;
	}
}
