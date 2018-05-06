package javelin.model.unit.condition;

import java.util.ArrayList;

import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.necromancy.Doom;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;

/**
 * @see Doom
 * @author alex
 */
public class Shaken extends Condition {

	public Shaken(float expireatp, Combatant c, Integer casterlevel) {
		super(expireatp, c, Effect.NEGATIVE, "shaken", casterlevel, 1);
	}

	@Override
	public void start(Combatant c) {
		c.source = c.source.clone();
		penalizeattacks(c.source.melee);
		penalizeattacks(c.source.ranged);
		c.source.fort -= 2;
		c.source.ref -= 2;
		c.source.addwill(-2);
	}

	private void penalizeattacks(ArrayList<AttackSequence> sequences) {
		for (AttackSequence sequence : sequences) {
			for (Attack a : sequence) {
				a.bonus -= 2;
			}
		}
	}

	@Override
	public void end(Combatant c) {
		// lasts at least one minute
	}

}
