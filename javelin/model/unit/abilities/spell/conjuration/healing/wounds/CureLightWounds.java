package javelin.model.unit.abilities.spell.conjuration.healing.wounds;

import javelin.controller.challenge.ChallengeRatingCalculator;

/**
 * See the d20 SRD for more info.
 */
public class CureLightWounds
		extends javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureModerateWounds {

	public CureLightWounds() {
		super("Cure light wounds", ChallengeRatingCalculator.ratespelllikeability(1),
				new int[] { 1, 8, 1 }, 1);
	}
}
