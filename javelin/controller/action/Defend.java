package javelin.controller.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.controller.action.ai.AiAction;
import javelin.controller.action.ai.AiMovement;
import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * Full Defense action. w is a better keyboard shortcut for it though.
 * 
 * @author alex
 */
public class Defend extends Action implements AiAction {
	public static final double APCOST = .5;
	public static final Action SINGLETON = new Defend();
	public static final boolean ALLOWAI = true;

	private Defend() {
		super("Defend (wait)", new String[] { "w", ".", "5" });
		allowwhileburrowed = true;
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

	@Override
	public boolean perform(Combatant active) {
		active.await();
		return true;
	}
}
