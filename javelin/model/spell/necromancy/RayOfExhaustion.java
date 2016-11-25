package javelin.model.spell.necromancy;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.Realm;
import javelin.model.condition.Condition;
import javelin.model.condition.Exhausted;
import javelin.model.condition.Fatigued;
import javelin.model.spell.Ray;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * Makes a creature exhausted or fatigued on a successful save.
 * 
 * @author alex
 */
public class RayOfExhaustion extends Ray {

	/** Constructor. */
	public RayOfExhaustion() {
		super("Ray of exhaustion", 3, ChallengeRatingCalculator.ratespelllikeability(3),
				Realm.EVIL);
		castinbattle = true;
		iswand = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		Condition c = !saved || target.hascondition(Fatigued.class) != null
				? new Exhausted(target, casterlevel)
				: new Fatigued(target, casterlevel, 0);
		target.addcondition(c);
		return target + " is " + c.description + ".";
	}

	@Override
	public int save(Combatant caster, Combatant target) {
		return rollsave(target.source.fortitude(), caster);
	}
}
