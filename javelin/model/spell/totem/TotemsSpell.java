package javelin.model.spell.totem;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.exception.NotPeaceful;
import javelin.model.spell.Ray;
import javelin.model.unit.Combatant;

/**
 * Common implementation of this type of spell.
 * 
 * @author alex
 */
public abstract class TotemsSpell extends Ray {

	public TotemsSpell(String name) {
		super(name, SpellsFactor.ratetouchconvertedtoray(2), false, true, 2);
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