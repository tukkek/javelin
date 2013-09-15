package javelin.model.spell;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.exception.NotPeaceful;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class Heroism extends Ray {
	public Heroism(String name) {
		super(name + "heroism", SpellsFactor
				.calculatechallengefortouchspellconvertedtoray(3), false, true,
				5);
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		if (target.hascondition(javelin.model.condition.Heroism.class)) {
			return "But " + target + " is already heroic!";
		}
		target.conditions.add(new javelin.model.condition.Heroism(target));
		return target + " is heroic!";
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
