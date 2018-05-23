package javelin.controller.action.ai.attack;

import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.Node;
import javelin.model.unit.Combatant;
import javelin.old.Game.Delay;
import javelin.view.mappanel.battle.overlay.AiOverlay;

public class DamageNode extends ChanceNode {
	public DamageNode(Node n, float chance, String action, Delay delay,
			Combatant target) {
		super(n, chance, action, delay);
		overlay = new AiOverlay(target.location[0], target.location[1]);
	}
}