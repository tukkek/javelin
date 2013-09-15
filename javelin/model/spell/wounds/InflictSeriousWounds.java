package javelin.model.spell.wounds;

import javelin.controller.challenge.factor.SpellsFactor;

public class InflictSeriousWounds extends InflictModerateWounds {

	public InflictSeriousWounds(String name, float incrementcost) {
		super(name, incrementcost, new int[] { 3, 5, 10 + 3 + 1 }, 5);
	}

	public InflictSeriousWounds(String string) {
		this(string + "inflict serious wounds", SpellsFactor
				.calculatechallengefortouchspellconvertedtoray(3));
	}

}
