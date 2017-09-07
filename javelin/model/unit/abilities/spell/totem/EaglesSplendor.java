package javelin.model.unit.abilities.spell.totem;

import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.totem.Splendid;

/**
 * See the d20 SRD for more info.
 */
public class EaglesSplendor extends TotemsSpell {

	public EaglesSplendor() {
		super("Eagle's splendor", Realm.AIR);
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		target.addcondition(new Splendid(target, casterlevel));
		return target + "'s charisma is now "
				+ Monster.getsignedbonus(target.source.charisma);
	}

}
