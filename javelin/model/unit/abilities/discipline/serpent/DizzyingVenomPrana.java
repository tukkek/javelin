package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.action.ActionCost;
import javelin.controller.action.ai.attack.DamageChance;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.WisdomDamage;

public class DizzyingVenomPrana extends Strike {
	public DizzyingVenomPrana() {
		super("Dizzying venom prana");
	}

	@Override
	public void prehit(Combatant active, Combatant target, Attack a,
			DamageChance dc, BattleState s) {
		target.ap += ActionCost.PARTIAL;
		int fort = target.source.fortitude();
		if (fort != Integer.MAX_VALUE
				&& fort + 10 >= 11 + getinitiationmodifier(active)) {
			target.addcondition(new WisdomDamage(target));
		}
	}

	@Override
	public void posthit(Combatant attacker, Combatant target, Attack a,
			DamageChance dc, BattleState s) {
		// no cleanup
	}

	@Override
	public void preattacks(Combatant current, Combatant target, Attack a,
			BattleState s) {
		// no cleanup
	}

	@Override
	public void postattacks(Combatant current, Combatant target, Attack a,
			BattleState s) {
		// no cleanup
	}
}
