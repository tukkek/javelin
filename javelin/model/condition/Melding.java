package javelin.model.condition;

import javelin.model.state.Meld;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;

/**
 * @see Meld
 * @author alex
 */
public class Melding extends Condition {
	/** Constructor. */
	public Melding(Combatant c) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, "melding", null);
	}

	@Override
	public void start(Combatant c) {
		c.hp += Math.ceil(c.maxhp / 5f);
		if (c.hp > c.maxhp) {
			c.hp = c.maxhp;
		}
		c.source = c.source.clone();
		c.source.ac += 2;
		for (AttackSequence s : c.source.melee) {
			for (Attack a : s) {
				a.bonus += 2;
			}
		}
		for (AttackSequence s : c.source.ranged) {
			for (Attack a : s) {
				a.bonus += 2;
			}
		}
	}

	@Override
	public void end(Combatant c) {
		// expires with battle
	}
}
