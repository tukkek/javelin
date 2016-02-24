package javelin.model.spell;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.exception.NotPeaceful;
import javelin.model.condition.Dominated;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * Based on the spell Dominate Monster but trades the duration (1 day/level) to
 * a single battle and to maintain spell-level balance cuts out all the costs of
 * redirecting and commanding the enchanted target.
 * 
 * It's not really a ray but we're abusing the existing logic here because it's
 * a lot easier.
 */
public class DominateMonster extends Ray {
	public static DominateMonster singleton = new DominateMonster();

	public DominateMonster() {
		super("Dominate monster", SpellsFactor.ratespelllikeability(9), false,
				false, 9);
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		if (saved) {
			return target + " resists!";
		}
		Dominated.switchteams(target, s);
		target.conditions.add(new Dominated(Float.MAX_VALUE, target));
		return "Dominated " + target + "!";
	}

	@Override
	public double apcost() {
		return 1;
	}

	@Override
	public int calculatetouchdc(Combatant combatant, Combatant target,
			BattleState s) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public int calculatehitdc(Combatant active, Combatant target,
			BattleState state) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public int calculatesavetarget(final Combatant caster,
			final Combatant target) {
		return save(9, target.source.will(), caster);
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant combatant)
			throws NotPeaceful {
		throw new NotPeaceful();
	}
}
