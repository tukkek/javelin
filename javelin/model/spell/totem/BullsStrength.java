package javelin.model.spell.totem;

import javelin.model.Realm;
import javelin.model.condition.totem.Strong;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

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
