package javelin.model.condition;

import javelin.model.spell.abjuration.ResistEnergy;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see ResistEnergy
 * @author alex
 */
public class Resistant extends Condition {
	int resistance;

	/**
	 * @param resistance
	 *            Number of {@link Monster#energyresistance} points.
	 * @param casterlevelp
	 */
	public Resistant(Combatant c, int resistance, Integer casterlevelp) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, "resistant", casterlevelp,
				1);
		this.resistance = resistance;
	}

	@Override
	public void start(Combatant c) {
		c.source.energyresistance += resistance;
	}

	@Override
	public void end(Combatant c) {
		c.source.energyresistance -= resistance;
	}

}
