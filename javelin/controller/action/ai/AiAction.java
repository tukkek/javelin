package javelin.controller.action.ai;

import java.util.List;

import javelin.controller.action.Action;
import javelin.controller.ai.ActionProvider;
import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * Implement this if you want {@link ActionProvider} to use your {@link Action}
 * as a possible AI move.
 */
public interface AiAction {
	/**
	 * Lists the possible results of an action.
	 */
	public List<List<ChanceNode>> getoutcomes(final BattleState s,
			final Combatant active);
}