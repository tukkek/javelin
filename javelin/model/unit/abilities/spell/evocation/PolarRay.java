package javelin.model.unit.abilities.spell.evocation;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Ray;

/**
 * A blue-white ray of freezing air and ice springs from your hand. The ray
 * deals 15d6 points of energy damage.
 * 
 * @author alex
 */
public class PolarRay extends Ray {
	/** Constructor. */
	public PolarRay() {
		super("Polar ray", 8, ChallengeCalculator.ratespelllikeability(8), Realm.AIR);
		castinbattle = true;
		isscroll = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, boolean saved,
			BattleState s, ChanceNode cn) {
		target.damage(15 * 6 / 2, s, target.source.energyresistance);
		return target + " is " + target.getstatus();
	}

}
