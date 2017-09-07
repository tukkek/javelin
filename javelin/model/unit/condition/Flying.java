package javelin.model.unit.condition;

import javelin.model.unit.abilities.spell.transmutation.Fly;
import javelin.model.unit.attack.Combatant;

/**
 * @see Fly
 * @author alex
 */
public class Flying extends Condition {

	int original;

	/**
	 * Constructor.
	 * 
	 * @param casterlevelp
	 */
	public Flying(Combatant c, Integer casterlevelp) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, "flying", casterlevelp);
	}

	@Override
	public void start(Combatant c) {
		original = c.source.fly;
		c.source.fly = 60;
	}

	@Override
	public void end(Combatant c) {
		c.source.fly = Math.min(c.source.fly, original);
	}

}
