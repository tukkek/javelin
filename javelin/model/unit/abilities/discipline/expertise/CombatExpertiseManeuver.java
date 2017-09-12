package javelin.model.unit.abilities.discipline.expertise;

import java.util.List;

import javelin.controller.action.Action;
import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.abilities.discipline.Maneuver;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.feat.Feat;
import javelin.model.unit.feat.attack.expertise.CombatExpertise;

/**
 * TODO may want to fully convert {@link CombatExpertise}-style {@link Feat}s to
 * an actual {@link Discipline} and {@link Maneuver}s at some point.
 * 
 * @author alex
 */
public abstract class CombatExpertiseManeuver extends Maneuver {
	public CombatExpertiseManeuver(String name, int level) {
		super(name, level);
	}

	@Override
	public void spend() {
		// doesn't
	}

	public abstract Action getaction();

	@Override
	public boolean perform(Combatant c) {
		return getaction().perform(c);
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(Combatant c, BattleState s,
			Maneuver m) {
		return ((AiAction) getaction()).getoutcomes(c, s);
	}
}