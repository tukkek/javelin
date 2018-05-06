package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.action.ActionCost;
import javelin.controller.action.ai.attack.DamageChance;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.condition.Condition;
import tyrant.mikera.engine.RPG;

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
	static final int DAMAGEBONUS = RPG.average(1, 4);

	public class RattlerSting extends Condition {
		public RattlerSting(float expireatp, Combatant c) {
			super(expireatp, c, Effect.NEGATIVE, "Rattler sting", null);
			stack = true;
		}

		@Override
		public void start(Combatant c) {
			// see #hit
		}

		@Override
		public void end(Combatant c) {
			c.damage(DAMAGEBONUS);
		}
	}

	public StingOfTheRattler() {
		super("Sting of the rattler", 1);
	}

	@Override
	public void prehit(Combatant active, Combatant target, Attack a,
			DamageChance dc, BattleState s) {
		dc.damage += DAMAGEBONUS;
		target.addcondition(
				new RattlerSting(active.ap + ActionCost.FULL, target));
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
