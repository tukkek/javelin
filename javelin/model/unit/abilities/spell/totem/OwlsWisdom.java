package javelin.model.unit.abilities.spell.totem;

import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.totem.Wise;

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
