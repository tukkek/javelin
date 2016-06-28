package javelin.model.spell.enchantment.compulsion;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.Realm;
import javelin.model.condition.Heroic;
import javelin.model.spell.Touch;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class Heroism extends Touch {
	public Heroism() {
		super("Heroism", 3, SpellsFactor.ratespelllikeability(3), Realm.FIRE);
		castonallies = true;
		castinbattle = true;
		ispotion = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		target.addcondition(new Heroic(target, casterlevel));
		return target + " is heroic!";
	}

}
