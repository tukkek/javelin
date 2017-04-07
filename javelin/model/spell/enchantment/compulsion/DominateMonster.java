package javelin.model.spell.enchantment.compulsion;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.Realm;
import javelin.model.condition.Dominated;
import javelin.model.spell.Ray;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * Based on the spell Dominate Monster but trades the duration (1 day/level) to
 * a single battle and to maintain spell-level balance cuts out all the costs of
 * redirecting and commanding the enchanted target.
 * 
 * It's not really a ray but we're abusing the existing logic here because it's
 * a lot easier.
 */
public class DominateMonster extends Ray {
	/** Constructor. */
	public DominateMonster() {
		super("Dominate monster", 9, ChallengeRatingCalculator.ratespelllikeability(9),
				Realm.EVIL);
		automatichit = true;
		apcost = 1;
		castinbattle = true;
		isscroll = true;
		apcost = 1;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		if (saved) {
			return target + " resists!";
		}
		Dominated.switchteams(target, s);
		target.addcondition(
				new Dominated(Float.MAX_VALUE, target, casterlevel));
		return "Dominated " + target + "!";
	}

	@Override
	public int save(final Combatant caster, final Combatant target) {
		return calculatesavedc(target.source.will(), caster);
	}

}
