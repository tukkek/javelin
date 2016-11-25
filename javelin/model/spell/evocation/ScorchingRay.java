package javelin.model.spell.evocation;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.Realm;
import javelin.model.spell.Ray;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * Deals 4d6 points of fire damage.
 * 
 * @author alex
 */
public class ScorchingRay extends Ray {

	public ScorchingRay() {
		super("Scorching ray", 2, ChallengeRatingCalculator.ratespelllikeability(2),
				Realm.FIRE);
		castinbattle = true;
		iswand = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		target.damage(4 * 6 / 2, s, target.source.energyresistance);
		return target + " is " + target.getstatus();
	}
}
