package javelin.model.unit.discipline.expertise;

import java.util.List;

import javelin.controller.action.Action;
import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ChanceNode;
import javelin.model.feat.Feat;
import javelin.model.feat.attack.expertise.CombatExpertise;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.discipline.Discipline;
import javelin.model.unit.discipline.Maneuver;

/**
 * TODO may want to fully convert {@link CombatExpertise}-style {@link Feat}s to
 * an actual {@link Discipline} and {@link Maneuver}s at some point.
 * 
 * @author alex
 */
public abstract class CombatExpertiseManeuver extends Maneuver {
	public CombatExpertiseManeuver(String name) {
		super(name);
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
	public List<List<ChanceNode>> getoutcomes(BattleState s, Combatant c) {
		return ((AiAction) getaction()).getoutcomes(s, c);
	}
}