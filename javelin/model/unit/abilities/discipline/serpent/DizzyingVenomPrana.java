package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.action.ActionCost;
import javelin.controller.action.ai.attack.DamageChance;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.WisdomDamage;

public class DizzyingVenomPrana extends Strike {
	public DizzyingVenomPrana() {
		super("Dizzying venom prana");
	}

	@Override
	public void hit(Combatant active, Combatant target, DamageChance dc,
			BattleState s) {
		target.ap += ActionCost.PARTIAL;
		int fort = target.source.fortitude();
		if (fort != Integer.MAX_VALUE
				&& fort + 10 >= 11 + getinitiationmodifier(active)) {
			target.addcondition(new WisdomDamage(target));
		}
	}
}
