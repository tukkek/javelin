package javelin.model.spell.necromancy.wounds;

import javelin.controller.challenge.factor.SpellsFactor;

/**
 * See the d20 SRD for more info.
 */
public class InflictCriticalWounds extends InflictModerateWounds {

	public InflictCriticalWounds(String name, float incrementcost) {
		super(name, incrementcost, new int[] { 4, 8, 7 }, 4);
	}

	public InflictCriticalWounds() {
		this("Inflict critical wounds", SpellsFactor.ratespelllikeability(4));
	}

}
