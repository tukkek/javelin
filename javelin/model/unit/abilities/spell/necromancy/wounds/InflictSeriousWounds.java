package javelin.model.unit.abilities.spell.necromancy.wounds;

import javelin.controller.challenge.ChallengeRatingCalculator;

/**
 * See the d20 SRD for more info.
 */
public class InflictSeriousWounds extends InflictModerateWounds {

	public InflictSeriousWounds(String name, float incrementcost) {
		super(name, incrementcost, new int[] { 3, 8, 5 }, 3);
	}

	public InflictSeriousWounds() {
		this("Inflict serious wounds", ChallengeRatingCalculator.ratespelllikeability(3));
	}

}
