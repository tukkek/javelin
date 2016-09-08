package javelin.controller.action.maneuver;

import javelin.controller.ai.ChanceNode;
import javelin.controller.old.Game.Delay;
import javelin.model.condition.Feigned;
import javelin.model.feat.attack.martial.ImprovedFeint;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

public class Feint extends Maneuver {
	public Feint() {
		super("Feint", "F", ImprovedFeint.SINGLETON, 0);
	}

	@Override
	boolean validatetarget(Combatant target) {
		return Monster.getbonus(target.source.dexterity) >= +1
				&& target.hascondition(Feigned.class) == null;
	}

	@Override
	ChanceNode hit(Combatant combatant, Combatant targetCombatant,
			BattleState battleState, float chance) {
		battleState = battleState.clone();
		targetCombatant = battleState.clone(targetCombatant);
		targetCombatant.addcondition(
				new Feigned(targetCombatant.ap + .1f, targetCombatant));
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
