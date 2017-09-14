package javelin.model.unit.abilities.spell.enchantment.compulsion;

import java.util.List;

import javelin.controller.challenge.CrCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.attack.Combatant;

public class BarbarianRage extends Rage {
	public BarbarianRage() {
		super("Barbarian rage", 1,
				CrCalculator.ratespelllikeability(1), Realm.FIRE);
		ispotion = false;
	}

	@Override
	float getduration(Combatant target) {
		return super.getduration(target) / 2f;
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		targetself(combatant, targets);
	}
}
