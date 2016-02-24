package javelin.model.spell.totem;

import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more info.
 */
public class BullsStrength extends TotemsSpell {

	public BullsStrength() {
		super("Bull's strength");
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		target.source = target.source.clone();
		target.source.raisestrength();
		target.source.raisestrength();
		target.source.strength += 4;
		return target + "'s strength is now "
				+ Monster.getsignedbonus(target.source.strength);
	}

}
