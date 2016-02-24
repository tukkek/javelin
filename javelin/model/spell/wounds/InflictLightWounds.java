package javelin.model.spell.wounds;

import javelin.controller.challenge.factor.SpellsFactor;

/**
 * See the d20 SRD for more info.
 */
public class InflictLightWounds extends InflictModerateWounds {

	public InflictLightWounds(String name, float incrementcost) {
		super(name, incrementcost, new int[] { 1, 8, 1 }, 1);
	}

	public InflictLightWounds() {
		this("Inflict light wounds", SpellsFactor.ratetouchconvertedtoray(1));
	}

}
