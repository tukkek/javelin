package javelin.controller.action.ai;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * To be used for debugging purposes.
 * 
 * @author alex
 */
public class ForceWait extends AiAction {

	public ForceWait() {
		super("Wait", new String[0]);
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(BattleState gameState,
			Combatant combatant) {
		ArrayList<List<ChanceNode>> list = new ArrayList<List<ChanceNode>>();
		list.add(AiMovement.wait(gameState, combatant));
		return list;
	}
}
