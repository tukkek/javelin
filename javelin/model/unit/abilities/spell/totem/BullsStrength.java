package javelin.model.unit.abilities.spell.totem;

import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.totem.Strong;

/**
 * See the d20 SRD for more info.
 */
public class BullsStrength extends TotemsSpell {

	public BullsStrength() {
		super("Bull's strength", Realm.FIRE);
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		target.addcondition(new Strong(target, casterlevel));
		return target + "'s strength is now "
				+ Monster.getsignedbonus(target.source.strength);
	}

}
