package javelin.model.spell.transmutation;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.Realm;
import javelin.model.spell.Touch;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class Fly extends Touch {
	/** Constructor. */
	public Fly() {
		super("Fly", 3, ChallengeRatingCalculator.ratespelllikeability(3), Realm.AIR);
		castinbattle = true;
		castonallies = true;
		ispotion = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		target.addcondition(
				new javelin.model.condition.Flying(target, casterlevel));
		return target + " floats above the ground!";
	}
}
