package javelin.model.spell.totem;

import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more info.
 */
public class CatsGrace extends TotemsSpell {

	public CatsGrace() {
		super("Cat's grace");
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		target.source = target.source.clone();
		target.source.raisedexterity(+2);
		target.source.dexterity += 4;
		return target + "'s dexterity is now "
				+ Monster.getsignedbonus(target.source.dexterity);
	}
}
