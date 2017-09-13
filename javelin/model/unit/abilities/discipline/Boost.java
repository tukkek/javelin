package javelin.model.unit.abilities.discipline;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.action.ActionCost;
import javelin.controller.ai.ChanceNode;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.unit.attack.Combatant;
import javelin.view.screen.BattleScreen;

public abstract class Boost extends Maneuver {
	public Boost(String name, int level) {
		super(name, level);
		ap = ActionCost.SWIFT;
	}

	@Override
	public boolean perform(Combatant c) {
		boost(c);
		c.ap += ap;
		BattleScreen.active.centerscreen(c.location[0], c.location[1]);
		Game.messagepanel.clear();
		Game.message(getaction(c), Delay.WAIT);
		return true;
	}

	abstract protected void boost(Combatant c);

	@Override
	public List<List<ChanceNode>> getoutcomes(Combatant c, BattleState s) {
		boost(c);
		c.ap += ap;
		ArrayList<ChanceNode> chance = new ArrayList<ChanceNode>(1);
		chance.add(new ChanceNode(s, 1, getaction(c), Delay.BLOCK));
		ArrayList<List<ChanceNode>> out = new ArrayList<List<ChanceNode>>(1);
		out.add(chance);
		return out;
	}

	String getaction(Combatant c) {
		return c + " uses " + name.toLowerCase() + "!";
	}
}
