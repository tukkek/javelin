package javelin.model.condition;

import javelin.model.spell.abjuration.Blink;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Provides {@link Monster#fly} as a menas of allowing units to pass through
 * solid objects.
 * 
 * @see Blink
 * @author alex
 */
public class Blinking extends Condition {
	int originalflight;

	public Blinking(float expireatp, Combatant c, Integer casterlevelp) {
		super(expireatp, c, Effect.POSITIVE, "blinking", casterlevelp);
	}

	@Override
	public void start(Combatant c) {
		c.acmodifier += 10;
		Monster m = c.source.clone();
		c.source = m;
		m.sr += 10;
		originalflight = m.fly;
		m.fly = Math.max(m.fly, m.walk);
	}

	@Override
	public void end(Combatant c) {
		c.acmodifier -= 10;
		Monster m = c.source.clone();
		c.source = m;
		m.sr -= 10;
		m.fly = Math.min(originalflight, m.fly);
	}
}
