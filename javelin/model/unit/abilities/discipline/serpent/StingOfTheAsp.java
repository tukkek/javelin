package javelin.model.unit.abilities.discipline.serpent;

import javelin.Javelin;
import javelin.controller.action.ActionCost;
import javelin.controller.action.ai.attack.DamageChance;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;
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
			c.hp -= EXTRADAMAGE;
			if (c.hp < 1) {
				c.hp = 1;
			}
			c.source = c.source.clone();
			c.source.changestrengthscore(-2);
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
		target.source = target.source.clone();
		boolean save = save(target.source.fortitude(), 12, active);
		target.source.changestrengthscore(save ? -1 : -2);
		if (!save || Javelin.DEBUG) {
			target.addcondition(new AspString(active.ap + 1, target));
		}
	}

	@Override
	public void posthit(Combatant current, Combatant target, Attack a,
			DamageChance dc, BattleState s) {

	}
}
