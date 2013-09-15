package javelin.model.condition;

import java.util.ArrayList;

import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;

public class Heroism extends Condition {
	public Heroism(final Combatant c) {
		super(Float.MAX_VALUE, c);
	}

	@Override
	void start(final Combatant c) {
		c.source = c.source.clone();
		raiseattacks(c.source.melee);
		raiseattacks(c.source.ranged);
		c.source.fort += 2;
		c.source.ref += 2;
		c.source.will += 2;
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

	@Override
	public String describe() {
		return "heroic";
	}
}
