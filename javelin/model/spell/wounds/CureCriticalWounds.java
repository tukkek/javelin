package javelin.model.spell.wounds;

import javelin.controller.challenge.factor.SpellsFactor;

/**
 * See the d20 SRD for more info.
 */
public class CureCriticalWounds extends CureModerateWounds {

	public CureCriticalWounds(String name, float incrementcost) {
		super(name, incrementcost, new int[] { 4, 8, 8 }, 4);
	}

	public CureCriticalWounds() {
		this("Cure critical wounds", SpellsFactor.ratetouchconvertedtoray(4));
	}

}
