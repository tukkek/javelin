package javelin.model.condition;

import javelin.model.spell.transmutation.Longstrider;
import javelin.model.unit.Combatant;

/**
 * @see Longstrider
 * @author alex
 */
public class Strider extends Condition {

	public Strider(Combatant c, Integer casterlevelp) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, "striding", casterlevelp, 1);
	}

	@Override
	public void start(Combatant c) {
		c.source = c.source.clone();
		c.source.walk += 10;
	}

	@Override
	public void end(Combatant c) {
		c.source.walk -= 10;
	}
}
