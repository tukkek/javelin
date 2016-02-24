package javelin.model.condition;

import javelin.model.spell.Blink;
import javelin.model.unit.Combatant;

/**
 * TODO walk through objects
 * 
 * TODO half area damage?
 * 
 * @see Blink
 * 
 * @author alex
 */
public class Blinking extends Condition {
	public Blinking(float expireatp, Combatant c) {
		super(expireatp, c, Effect.POSITIVE, "blinking");
	}

	@Override
			void start(Combatant c) {
		c.acmodifier += 10;
		c.source = c.source.clone();
		c.source.sr += 10;
	}

	@Override
			void end(Combatant c) {
		c.acmodifier -= 10;
		c.source = c.source.clone();
		c.source.sr -= 10;
	}
}
