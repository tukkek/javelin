package javelin.model.spell.totem;

import javelin.model.Realm;
import javelin.model.condition.totem.Wise;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more info.
 */
public class OwlsWisdom extends TotemsSpell {

	public OwlsWisdom() {
		super("Owl's wisdom", Realm.GOOD);
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		target.addcondition(new Wise(target, casterlevel));
		return target + "'s wisdom is now "
				+ Monster.getsignedbonus(target.source.wisdom);
	}

}
