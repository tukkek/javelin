package javelin.model.condition;

import javelin.model.spell.abjuration.Blink;
import javelin.model.unit.Combatant;

/**
 * @see Blink
 * @author alex
 */
public class Blinking extends Condition {
	public Blinking(float expireatp, Combatant c, Integer casterlevelp) {
		super(expireatp, c, Effect.POSITIVE, "blinking", casterlevelp);
	}

	@Override
	public void start(Combatant c) {
		c.source = c.source.clone();
		c.source.misschance += .5;
	}

	@Override
	public void end(Combatant c) {
		c.source = c.source.clone();
		c.source.misschance -= .5;
	}
}
