package javelin.model.spell;

import javelin.controller.exception.NotPeaceful;
import javelin.controller.upgrade.Spell;
import javelin.model.condition.Paralyzed;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class HoldMonster extends Spell {

	public HoldMonster() {
		super("Hold monster", .45f, false, 5, false);
	}

	@Override
	public int calculatetouchdc(Combatant combatant, Combatant targetCombatant,
			BattleState s) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		if (saved) {
			return target + " resists.";
		}
		int turns = save(5, 0, caster) - 10 - target.source.will();
		if (turns > 9) {
			turns = 9;
		} else if (turns < 1) {
			turns = 1;
		}
		target.conditions.add(new Paralyzed(caster.ap + turns, target));
		return target + " is paralyzed for " + turns + " turns!";
	}

	@Override
	public int calculatehitdc(Combatant active, Combatant target,
			BattleState state) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public int calculatesavetarget(Combatant caster, Combatant target) {
		return save(5, target.source.will(), caster);
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant combatant)
			throws NotPeaceful {
		throw new NotPeaceful();
	}

}
