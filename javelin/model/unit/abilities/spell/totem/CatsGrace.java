package javelin.model.unit.abilities.spell.totem;

import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.totem.Graceful;

/**
 * See the d20 SRD for more info.
 */
public class CatsGrace extends TotemsSpell {

	public CatsGrace() {
		super("Cat's grace", Realm.WATER);
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		target.addcondition(new Graceful(target, casterlevel));
		return target + "'s dexterity is now "
				+ Monster.getsignedbonus(target.source.dexterity) + "!";
	}
}
