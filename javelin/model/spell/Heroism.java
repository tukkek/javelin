package javelin.model.spell;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.exception.NotPeaceful;
import javelin.model.condition.Heroic;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class Heroism extends Ray {
	public Heroism() {
		super("Heroism", SpellsFactor.ratetouchconvertedtoray(3), false, true,
				3);
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		return makeheroic(target);
	}

	static public String makeheroic(Combatant target) {
		if (target.hascondition(Heroic.class)) {
			return "But " + target + " is already heroic...";
		}
		target.conditions.add(new Heroic(target));
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
