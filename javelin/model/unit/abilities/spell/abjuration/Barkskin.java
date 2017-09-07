package javelin.model.unit.abilities.spell.abjuration;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.attack.Combatant;

/**
 * Gives a 1-hour bonus to
 * 
 * See the d20 SRD for more info.
 */
public class Barkskin extends Touch {
	/** Constructor */
	public Barkskin() {
		super("Barkskin", 3, ChallengeRatingCalculator.ratespelllikeability(2, 6),
				javelin.model.Realm.EARTH);
		casterlevel = 6;
		castinbattle = true;
		castoutofbattle = true;
		castonallies = true;
		ispotion = true;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		target.addcondition(
				new javelin.model.unit.condition.Barkskin(target, casterlevel));
		return target + " now has an armor class of " + target.ac() + "!";
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		return castpeacefully(caster, target);
	}
}