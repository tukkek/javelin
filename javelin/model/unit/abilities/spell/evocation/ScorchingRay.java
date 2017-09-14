package javelin.model.unit.abilities.spell.evocation;

import javelin.controller.challenge.CrCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Ray;
import javelin.model.unit.attack.Combatant;

/**
 * Deals 4d6 points of fire damage.
 * 
 * @author alex
 */
public class ScorchingRay extends Ray {

	public ScorchingRay() {
		super("Scorching ray", 2, CrCalculator.ratespelllikeability(2),
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
