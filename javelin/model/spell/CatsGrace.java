package javelin.model.spell;

import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class CatsGrace extends TotemsSpell {

	public CatsGrace(final String name) {
		super(name + "cat's grace");
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		target.source = target.source.clone();
		target.source.raisedexterity(+2);
		target.source.dexterity += 4;
		return target + "'s dexterity is now " + target.source.dexterity;
	}
}
