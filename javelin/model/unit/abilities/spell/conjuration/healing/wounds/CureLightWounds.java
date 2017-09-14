package javelin.model.unit.abilities.spell.conjuration.healing.wounds;

import javelin.controller.challenge.CrCalculator;

/**
 * See the d20 SRD for more info.
 */
public class CureLightWounds
		extends javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureModerateWounds {

	public CureLightWounds() {
		super("Cure light wounds", CrCalculator.ratespelllikeability(1),
				new int[] { 1, 8, 1 }, 1);
	}
}
