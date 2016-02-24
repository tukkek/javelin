package javelin.controller.action.maneuver;

import javelin.controller.ai.ChanceNode;
import javelin.model.condition.Feigned;
import javelin.model.feat.ImprovedFeint;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import tyrant.mikera.tyrant.Game.Delay;

public class Feint extends Maneuver {
	public Feint() {
		super("Feint", "F", 'F', ImprovedFeint.singleton, 0);
	}

	@Override
			boolean validatetarget(Combatant target) {
		return Monster.getbonus(target.source.dexterity) >= +1
				&& !target.hascondition(Feigned.class);
	}

	@Override
			ChanceNode hit(Combatant combatant, Combatant targetCombatant,
					BattleState battleState, float chance) {
		battleState = battleState.clone();
		targetCombatant = battleState.clone(targetCombatant);
		targetCombatant.conditions
				.add(new Feigned(targetCombatant.ap + .1f, targetCombatant));
		return new ChanceNode(battleState, chance, "Feint succesfull!",
				Delay.BLOCK);
	}

	@Override
			int getsavebonus(Combatant targetCombatant) {
		return targetCombatant.source.will();
	}

	@Override
			int getattackerbonus(Combatant combatant) {
		return Monster.getbonus(combatant.source.intelligence);
	}

	@Override
			ChanceNode miss(Combatant combatant, Combatant target,
					BattleState battleState, float chance) {
		return new ChanceNode(battleState, chance, "Feint attemp fails...",
				Delay.WAIT);
	}
}
