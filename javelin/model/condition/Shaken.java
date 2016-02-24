package javelin.model.condition;

import java.util.ArrayList;

import javelin.model.spell.Doom;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;

/**
 * @see Doom
 * @author alex
 */
public class Shaken extends Condition {

	public Shaken(float expireatp, Combatant c) {
		super(expireatp, c, Effect.NEGATIVE, "shaken");
	}

	@Override
			void start(Combatant c) {
		if (c.hascondition(Shaken.class)) {
			return;
		}
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
			void end(Combatant c) {
		// lasts at least one minute
	}

}
