package javelin.model.unit.abilities.spell.necromancy;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Ray;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.condition.Exhausted;
import javelin.model.unit.condition.Fatigued;

/**
 * Makes a creature exhausted or fatigued on a successful save.
 * 
 * @author alex
 */
public class RayOfExhaustion extends Ray {

	/** Constructor. */
	public RayOfExhaustion() {
		super("Ray of exhaustion", 3, ChallengeCalculator.ratespelllikeability(3),
				Realm.EVIL);
		castinbattle = true;
		iswand = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, boolean saved,
			BattleState s, ChanceNode cn) {
		Condition c = !saved || target.hascondition(Fatigued.class) != null
				? new Exhausted(target, casterlevel)
				: new Fatigued(target, casterlevel, 0);
		target.addcondition(c);
		return target + " is " + c.description + ".";
	}

	@Override
	public int save(Combatant caster, Combatant target) {
		return calculatesavedc(target.source.fortitude(), caster);
	}
}
