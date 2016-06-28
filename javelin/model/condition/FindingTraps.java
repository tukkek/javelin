package javelin.model.condition;

import javelin.model.spell.divination.FindTraps;
import javelin.model.unit.Combatant;

/**
 * @see FindTraps
 * @author alex
 */
public class FindingTraps extends Condition {

	/**
	 * Constructor.
	 * 
	 * @param casterlevelp
	 */
	public FindingTraps(Combatant c, Integer casterlevelp) {
		super(Float.MAX_VALUE, c, Effect.NEUTRAL, "finding traps", casterlevelp,
				1);
	}

	@Override
	public void start(Combatant c) {
		c.source.skills.search += 3;
	}

	@Override
	public void end(Combatant c) {
		c.source.skills.search -= 3;
	}
}
