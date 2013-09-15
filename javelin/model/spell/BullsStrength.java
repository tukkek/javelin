package javelin.model.spell;

import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class BullsStrength extends TotemsSpell {

	public BullsStrength(final String name) {
		super(name + "bull's strength");
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		target.source = target.source.clone();
		target.source.raisestrength();
		target.source.raisestrength();
		target.source.strength += 4;
		return target + "'s strength is now " + target.source.strength;
	}

}
