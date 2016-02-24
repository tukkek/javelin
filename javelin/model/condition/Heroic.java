package javelin.model.condition;

import java.util.ArrayList;

import javelin.model.item.potion.Heroism;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;

/**
 * @see javelin.model.spell.Heroism
 * @see Heroism
 * @author alex
 */
public class Heroic extends Condition {
	public Heroic(final Combatant c) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, "heroic");
	}

	@Override
			void start(final Combatant c) {
		c.source = c.source.clone();
		raiseattacks(c.source.melee);
		raiseattacks(c.source.ranged);
		c.source.fort += 2;
		c.source.ref += 2;
		c.source.addwill(2);
	}

	public void raiseattacks(ArrayList<AttackSequence> melee) {
		for (final AttackSequence sequence : melee) {
			for (final Attack a : sequence) {
				a.bonus += 2;
			}
		}
	}

	@Override
			void end(final Combatant c) {
		// expires at the end of combat
	}

}
