package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.action.ActionCost;
import javelin.controller.action.ai.attack.DamageChance;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;

/**
 * Attack that inflicts an additional 1d4 points of damage plus 1d4 damage the
 * following round.
 * 
 * TODO would be nicer to be able to kill the target on secondary damage but
 * we're not passing {@link BattleState} parameters as of yet.
 * 
 * @author alex
 */
public class StingOfTheRattler extends Strike {
	public class Stung extends Condition {
		public Stung(float expireatp, Combatant c) {
			super(expireatp, c, Effect.NEGATIVE, "Stung", null);
		}

		@Override
		public void start(Combatant c) {
			// see #hit
		}

		@Override
		public void end(Combatant c) {
			c.hp -= 3;
			if (c.hp < 1) {
				c.hp = 1;
			}
		}
	}

	public StingOfTheRattler() {
		super("Sting of the rattler");
	}

	@Override
	public void prehit(Combatant active, Combatant target, Attack a,
			DamageChance dc, BattleState s) {
		dc.damage += 3;
		target.addcondition(new Stung(active.ap + ActionCost.FULL, target));
	}

	@Override
	public void posthit(Combatant attacker, Combatant target, Attack a,
			DamageChance dc, BattleState s) {
		// no cleanup
	}

	@Override
	public void preattacks(Combatant current, Combatant target, Attack a,
			BattleState s) {
		// nothing
	}

	@Override
	public void postattacks(Combatant current, Combatant target, Attack a,
			BattleState s) {
		// nothing
	}
}
