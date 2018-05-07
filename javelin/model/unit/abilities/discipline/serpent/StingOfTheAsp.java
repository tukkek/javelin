package javelin.model.unit.abilities.discipline.serpent;

import javelin.Javelin;
import javelin.controller.action.ActionCost;
import javelin.controller.action.ai.attack.DamageChance;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.condition.abilitydamage.StrengthDamage;
import tyrant.mikera.engine.RPG;

/**
 * TODO should allow combatant to be killed by secondary damage
 * 
 * @author alex
 */
public class StingOfTheAsp extends Strike {
	static final int EXTRADAMAGE = RPG.average(1, 6);

	public class AspString extends Condition {
		public AspString(float expireatp, Combatant c) {
			super(expireatp, c, Effect.NEGATIVE, "Asp sting", null);
			stack = true;
		}

		@Override
		public void start(Combatant c) {
			// nothing at first
		}

		@Override
		public void end(Combatant c) {
			c.damage(EXTRADAMAGE);
			c.source = c.source.clone();
			c.addcondition(new StrengthDamage(2, c));
		}
	}

	public StingOfTheAsp() {
		super("Sting of the asp", 2);
		ap = ActionCost.STANDARD;
	}

	@Override
	public void preattacks(Combatant current, Combatant target, Attack a,
			BattleState s) {

	}

	@Override
	public void postattacks(Combatant current, Combatant target, Attack a,
			BattleState s) {

	}

	@Override
	public void prehit(Combatant active, Combatant target, Attack a,
			DamageChance dc, BattleState s) {
		dc.damage += EXTRADAMAGE;
		boolean save = save(target.source.fortitude(), 12, active);
		target.addcondition(new StrengthDamage(save ? 1 : 2, target));
		if (!save || Javelin.DEBUG) {
			target.addcondition(new AspString(active.ap + 1, target));
		}
	}

	@Override
	public void posthit(Combatant current, Combatant target, Attack a,
			DamageChance dc, BattleState s) {

	}
}
