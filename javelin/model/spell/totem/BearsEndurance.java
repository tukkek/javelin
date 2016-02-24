package javelin.model.spell.totem;

import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more info.
 */
public class BearsEndurance extends TotemsSpell {

	public BearsEndurance() {
		super("Bear's endurance");
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		target.source = target.source.clone();
		target.source.raiseconstitution(target);
		target.source.raiseconstitution(target);
		target.source.constitution += 4;
		return target + "'s constitution is now "
				+ Monster.getsignedbonus(target.source.constitution);
	}

}
