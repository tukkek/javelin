package javelin.model.unit.condition;

import javelin.model.state.Meld;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.attack.Combatant;

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
		c.heal(Math.round(Math.round(Math.ceil(c.maxhp / 5f))), true);
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
