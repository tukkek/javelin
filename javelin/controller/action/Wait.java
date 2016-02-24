package javelin.controller.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.controller.action.ai.AiAction;
import javelin.controller.action.ai.AiMovement;
import javelin.controller.ai.ChanceNode;
import javelin.model.condition.Defending;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * Technically it's the action Full Defense because it gives a bonus to AC (see
 * {@link Defending} but w is a better keyboard shortcut for it.
 * 
 * Waiting is disabled by default, pending further testing. Initially the AI
 * decided to wait all the time. The exceptin is when she is wut of
 * {@link AiMovement} TODO
 * 
 * @author alex
 */
public class Wait extends Action implements AiAction {
	public static final double APCOST = .5;
	public static final Action SINGLETON = new Wait();
	public static final boolean ALLOWAI = true;

	private Wait() {
		super("Wait");
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(final BattleState gameState,
			final Combatant active) {
		if (!ALLOWAI) {
			return Collections.emptyList();
		}
		ArrayList<List<ChanceNode>> list = new ArrayList<List<ChanceNode>>();
		list.add(AiMovement.wait(gameState, active));
		return list;
	}
}
