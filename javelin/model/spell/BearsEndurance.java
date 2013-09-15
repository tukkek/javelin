package javelin.model.spell;

import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class BearsEndurance extends TotemsSpell {

	public BearsEndurance(final String name) {
		super(name + "bear's endurance");
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		target.source = target.source.clone();
		target.source.raiseconstitution(target);
		target.source.raiseconstitution(target);
		target.source.constitution += 4;
		return target + "'s constitution is now " + target.source.constitution;
	}

}
