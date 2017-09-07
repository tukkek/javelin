package javelin.model.unit.abilities.spell.evocation;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;

/**
 * Deals 1d4+1 points of force damage.
 * 
 * @author alex
 */
public class MagicMissile extends Spell {
	public MagicMissile() {
		super("Magic missile", 1, ChallengeRatingCalculator.ratespelllikeability(1),
				Realm.FIRE);
		castinbattle = true;
		iswand = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		target.damage(1 * 4 / 2 + 1, s, 0);
		return target + " is " + target.getstatus();
	}

	@Override
	public int save(Combatant caster, Combatant target) {
		return -Integer.MAX_VALUE;
	}
}
