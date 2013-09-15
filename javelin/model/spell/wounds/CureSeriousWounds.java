package javelin.model.spell.wounds;

import javelin.controller.challenge.factor.SpellsFactor;

public class CureSeriousWounds extends CureModerateWounds {

	public CureSeriousWounds(String name, float incrementcost) {
		super(name, incrementcost, new int[] { 3, 8, 6 }, 5);
	}

	public CureSeriousWounds(String string) {
		this(string + "cure serious wounds", SpellsFactor
				.calculatechallengefortouchspellconvertedtoray(3));
	}
}
