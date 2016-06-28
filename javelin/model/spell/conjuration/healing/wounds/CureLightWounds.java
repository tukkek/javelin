package javelin.model.spell.conjuration.healing.wounds;

import javelin.controller.challenge.factor.SpellsFactor;

/**
 * See the d20 SRD for more info.
 */
public class CureLightWounds
		extends javelin.model.spell.conjuration.healing.wounds.CureModerateWounds {

	public CureLightWounds() {
		super("Cure light wounds", SpellsFactor.ratespelllikeability(1),
				new int[] { 1, 8, 1 }, 1);
	}
}
