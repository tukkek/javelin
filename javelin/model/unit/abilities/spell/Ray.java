package javelin.model.unit.abilities.spell;

import javelin.controller.action.ai.attack.RangedAttack;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * A ranged touch attack spell.
 * 
 * @author alex
 */
public abstract class Ray extends Spell {

	public Ray(String name, int level, float incrementcost, Realm realmp) {
		super(name, level, incrementcost, realmp);
	}

	@Override
	public int hit(Combatant active, Combatant target,
			BattleState state) {
		if (automatichit) {
			return -Integer.MAX_VALUE;
		}
		int bonus = active.source.getbaseattackbonus()
				+ Monster.getbonus(active.source.dexterity);
		int ac;
		if (castonallies) {
			if (Walker.distance(active, target) < 2) {
				return -Integer.MAX_VALUE;
			}
			ac = 10;
		} else {
			ac = target.ac() - target.source.armor;
			bonus -= RangedAttack.SINGLETON.getpenalty(active, target, state);
		}
		return ac - bonus;
	}
}