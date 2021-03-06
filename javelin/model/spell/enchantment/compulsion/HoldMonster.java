package javelin.model.spell.enchantment.compulsion;

import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.condition.Paralyzed;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class HoldMonster extends Spell {

	public HoldMonster() {
		super("Hold monster", 5, .45f, Realm.MAGIC);
		castinbattle = true;
		isscroll = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		if (saved) {
			return target + " resists.";
		}
		int turns = rollsave(0, caster) - 10 - target.source.will();
		if (turns > 9) {
			turns = 9;
		} else if (turns < 1) {
			turns = 1;
		}
		target.addcondition(
				new Paralyzed(caster.ap + turns, target, casterlevel));
		return target + " is paralyzed for " + turns + " turns!";
	}

	@Override
	public int save(Combatant caster, Combatant target) {
		if (target.source.immunitytoparalysis) {
			return -Integer.MAX_VALUE;
		}
		return rollsave(target.source.will(), caster);
	}
}
