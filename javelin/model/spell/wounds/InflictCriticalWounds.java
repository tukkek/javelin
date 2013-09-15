package javelin.model.spell.wounds;

import javelin.controller.challenge.factor.SpellsFactor;

public class InflictCriticalWounds extends InflictModerateWounds {

	public InflictCriticalWounds(String name, float incrementcost) {
		super(name, incrementcost, new int[] { 4, 7, 10 + 4 + 2 }, 7);
	}

	public InflictCriticalWounds(String prefix) {
		this(prefix + "inflict critical wounds", SpellsFactor
				.calculatechallengefortouchspellconvertedtoray(4));
	}

}
