package javelin.model.spell.necromancy.wounds;

import javelin.controller.challenge.factor.SpellsFactor;

/**
 * See the d20 SRD for more info.
 */
public class InflictSeriousWounds extends InflictModerateWounds {

	public InflictSeriousWounds(String name, float incrementcost) {
		super(name, incrementcost, new int[] { 3, 8, 5 }, 3);
	}

	public InflictSeriousWounds() {
		this("Inflict serious wounds", SpellsFactor.ratespelllikeability(3));
	}

}
