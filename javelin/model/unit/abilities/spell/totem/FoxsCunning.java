package javelin.model.unit.abilities.spell.totem;

import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.totem.Cunning;

/**
 * See the d20 SRD for more info.
 */
public class FoxsCunning extends TotemsSpell {

	public FoxsCunning() {
		super("Fox's cunning", Realm.EVIL);
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		target.addcondition(new Cunning(target, casterlevel));
		return target + "'s intelligence is now "
				+ Monster.getsignedbonus(target.source.intelligence);
	}

}
