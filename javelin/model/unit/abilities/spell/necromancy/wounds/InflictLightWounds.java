package javelin.model.unit.abilities.spell.necromancy.wounds;

import javelin.controller.challenge.ChallengeCalculator;

/**
 * See the d20 SRD for more info.
 */
public class InflictLightWounds extends InflictModerateWounds {

	public InflictLightWounds(String name, float incrementcost) {
		super(name, incrementcost, new int[] { 1, 8, 1 }, 1);
	}

	public InflictLightWounds() {
		this("Inflict light wounds", ChallengeCalculator.ratespelllikeability(1));
	}

}
