package javelin.model.spell.conjuration.healing.wounds;

import javelin.controller.challenge.factor.SpellsFactor;

/**
 * See the d20 SRD for more info.
 */
public class CureSeriousWounds extends CureModerateWounds {

	public CureSeriousWounds(String name, float incrementcost) {
		super(name, incrementcost, new int[] { 3, 8, 6 }, 3);
	}

	public CureSeriousWounds() {
		this("Cure serious wounds", SpellsFactor.ratespelllikeability(3));
	}
}
