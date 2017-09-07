package javelin.model.unit.condition;

import javelin.model.unit.attack.Combatant;

/**
 * @see javelin.model.unit.abilities.spell.abjuration.Barkskin
 * @author alex
 */
public class Barkskin extends Condition {

	/**
	 * Constructor.
	 * 
	 * @param casterlevelp
	 */
	public Barkskin(Combatant c, Integer casterlevelp) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, "barkskin", casterlevelp, 1);
	}

	@Override
	public void start(Combatant c) {
		c.acmodifier += 3;
	}

	@Override
	public void end(Combatant c) {
		c.acmodifier -= 3;
	}
}
