package javelin.model.spell.wounds;

import javelin.controller.challenge.factor.SpellsFactor;

/**
 * See the d20 SRD for more info.
 */
public class CureLightWounds
		extends javelin.model.spell.wounds.CureModerateWounds {

	public CureLightWounds() {
		super("Cure light wounds", SpellsFactor.ratetouchconvertedtoray(1),
				new int[] { 1, 8, 1 }, 1);
	}
}
