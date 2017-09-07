package javelin.model.unit.abilities.spell.necromancy.wounds;

import javelin.controller.challenge.ChallengeRatingCalculator;

/**
 * See the d20 SRD for more info.
 */
public class InflictCriticalWounds extends InflictModerateWounds {

	public InflictCriticalWounds(String name, float incrementcost) {
		super(name, incrementcost, new int[] { 4, 8, 7 }, 4);
	}

	public InflictCriticalWounds() {
		this("Inflict critical wounds", ChallengeRatingCalculator.ratespelllikeability(4));
	}

}
