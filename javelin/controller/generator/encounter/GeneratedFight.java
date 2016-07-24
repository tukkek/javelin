package javelin.controller.generator.encounter;

import java.util.ArrayList;

import javelin.model.unit.Combatant;

/**
 * TODO probably can be refactored into an {@link Encounter} even though they
 * are being used for different purposes.
 */
public class GeneratedFight {
	public ArrayList<Combatant> opponents;

	public GeneratedFight(final ArrayList<Combatant> opponenets) {
		opponents = opponenets;
	}
}
