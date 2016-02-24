package javelin.model.spell;

import javelin.controller.action.ai.AbstractAttack;
import javelin.controller.action.ai.RangedAttack;
import javelin.controller.upgrade.Spell;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * A ranged touch attack spell.
 * 
 * @author alex
 */
public abstract class Ray extends Spell {

	public Ray(String name, float incrementcost, boolean ispeacefulp,
			boolean friendlyp, int level) {
		super(name, incrementcost, ispeacefulp, level, friendlyp);
	}

	@Override
	public int calculatetouchdc(final Combatant combatant,
			final Combatant target, final BattleState s) {
		return calculatehitdc(combatant, target, s);
	}

	@Override
	public int calculatehitdc(Combatant active, Combatant target,
			BattleState state) {
		final int ac;
		long bonus = active.source.getbaseattackbonus()
				- AbstractAttack.waterpenalty(state, active);
		if (friendly) {
			if (Walker.distance(active, target) < 2) {
				return -Integer.MAX_VALUE;
			}
			ac = 10;
		} else {
			ac = target.ac() - target.source.armor;
			bonus += AbstractAttack.waterpenalty(state, target)
					- RangedAttack.penalize(active, target, state);
		}
		return new Long(ac - (bonus + Monster.getbonus(active.source.dexterity)
				- RangedAttack.penalize(active, target, state))).intValue();
	}
}