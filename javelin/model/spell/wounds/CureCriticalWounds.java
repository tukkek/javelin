package javelin.model.spell.wounds;

import javelin.controller.challenge.factor.SpellsFactor;

public class CureCriticalWounds extends CureModerateWounds {

	public CureCriticalWounds(String name, float incrementcost) {
		super(name, incrementcost, new int[] { 4, 8, 8 }, 7);
	}

	public CureCriticalWounds(String string) {
		this(string + "cure critical wounds", SpellsFactor
				.calculatechallengefortouchspellconvertedtoray(4));
	}

}
