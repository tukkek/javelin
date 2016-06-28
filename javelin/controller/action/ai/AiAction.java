package javelin.controller.action.ai;

import java.util.List;

import javelin.controller.action.Action;
import javelin.controller.ai.ActionProvider;
import javelin.controller.ai.ChanceNode;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import tyrant.mikera.engine.Thing;

/**
 * Use this if you want {@link ActionProvider} to use your {@link Action} as a
 * possible AI move.
 */
public abstract class AiAction extends Action {

	/** See {@link Action#Action(String)}. */
	public AiAction(String string) {
		super(string);
	}

	public AiAction(String string, String[] strings) {
		super(string, strings);
	}

	public AiAction(String name, String key) {
		super(name, key);
	}

	/**
	 * Lists the possible results of an action.
	 */
	public abstract List<List<ChanceNode>> getoutcomes(final BattleState s,
			final Combatant active);

	@Override
	public boolean perform(Combatant active, BattleMap m, Thing thing) {
		return false;
	}
}