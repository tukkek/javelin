package javelin.model.unit.abilities.spell.evocation;

import javelin.controller.challenge.CrCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Ray;
import javelin.model.unit.attack.Combatant;

/**
 * A blue-white ray of freezing air and ice springs from your hand. The ray
 * deals 15d6 points of energy damage.
 * 
 * @author alex
 */
public class PolarRay extends Ray {
	/** Constructor. */
	public PolarRay() {
		super("Polar ray", 8, CrCalculator.ratespelllikeability(8), Realm.AIR);
		castinbattle = true;
		isscroll = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		target.damage(15 * 6 / 2, s, target.source.energyresistance);
		return target + " is " + target.getstatus();
	}

}
