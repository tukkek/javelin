package javelin.model.unit.abilities.spell.enchantment.compulsion;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Heroic;

/**
 * See the d20 SRD for more info.
 */
public class Heroism extends Touch {
	public Heroism() {
		super("Heroism", 3, ChallengeRatingCalculator.ratespelllikeability(3), Realm.FIRE);
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
