package javelin.controller.action.ai;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.action.Action;
import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.attack.Combatant;

/**
 * To be used for debugging purposes.
 * 
 * @author alex
 */
public class ForceWait extends Action implements AiAction {

	public ForceWait() {
		super("Wait", new String[0]);
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(Combatant combatant,
			BattleState gameState) {
		ArrayList<List<ChanceNode>> list = new ArrayList<List<ChanceNode>>();
		list.add(AiMovement.wait(gameState, combatant));
		return list;
	}

	@Override
	public boolean perform(Combatant active) {
		return false;
	}
}
