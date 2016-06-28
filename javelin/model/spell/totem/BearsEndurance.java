package javelin.model.spell.totem;

import javelin.model.Realm;
import javelin.model.condition.totem.Enduring;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more info.
 */
public class BearsEndurance extends TotemsSpell {

	public BearsEndurance() {
		super("Bear's endurance", Realm.EARTH);
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		target.addcondition(new Enduring(target, casterlevel));
		return target + "'s constitution is now "
				+ Monster.getsignedbonus(target.source.constitution);
	}

}
