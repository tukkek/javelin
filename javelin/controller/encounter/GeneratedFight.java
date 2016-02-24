package javelin.controller.encounter;

import java.util.List;

import javelin.model.unit.Combatant;

/**
 * TODO probably can be refactored into an {@link Encounter} even though they
 * are being used for different purposes.
 */
public class GeneratedFight {
	public List<Combatant> opponents;

	public GeneratedFight(final List<Combatant> opponenets) {
		opponents = opponenets;
	}
}
