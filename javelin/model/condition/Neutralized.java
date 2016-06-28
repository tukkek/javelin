package javelin.model.condition;

import javelin.model.spell.conjuration.healing.NeutralizePoison;
import javelin.model.unit.Combatant;

/**
 * @see NeutralizePoison
 * @see Poisoned
 * @author alex
 */
public class Neutralized extends Condition {
	/**
	 * Constructor.
	 * 
	 * @param casterlevelp
	 */
	public Neutralized(Combatant c, Integer casterlevelp) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, "immune to poison",
				casterlevelp, 1);
	}

	@Override
	public void start(Combatant c) {
		Poisoned p = (Poisoned) c.hascondition(Poisoned.class);
		if (p != null) {
			p.neutralized = true;
			c.removecondition(p);
		}
	}

	@Override
	public void end(Combatant c) {
		// does nothing
	}
}
