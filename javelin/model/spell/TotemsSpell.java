package javelin.model.spell;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.exception.NotPeaceful;
import javelin.model.unit.Combatant;

public abstract class TotemsSpell extends Ray {

	public TotemsSpell(String name) {
		super(name, SpellsFactor
				.calculatechallengefortouchspellconvertedtoray(2), false, true,
				3);
	}

	@Override
	public int calculatesavetarget(Combatant caster, Combatant target) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant combatant)
			throws NotPeaceful {
		throw new NotPeaceful();
	}

}