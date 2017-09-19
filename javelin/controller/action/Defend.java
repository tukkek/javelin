package javelin.controller.action;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ChanceNode;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Defending;

/**
 * Full Defense action.
 * 
 * @see Combatant#await()
 * @author alex
 */
public class Defend extends Action implements AiAction {
	/** Unique instance of this class. */
	public static final Action SINGLETON = new Defend();
	/** Defense cost in action points: {@value #APCOST}. */
	public static final double APCOST = .5;

	private Defend() {
		super("Defend (wait)", new String[] { "w", ".", "5", " " });
		allowburrowed = true;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(final Combatant c,
			final BattleState s) {
		ArrayList<List<ChanceNode>> list = new ArrayList<List<ChanceNode>>();
		list.add(wait(c, s));
		return list;
	}

	@Override
	public boolean perform(Combatant active) {
		defend(active);
		return true;
	}

	ArrayList<ChanceNode> wait(final Combatant c, final BattleState s) {
		final ArrayList<ChanceNode> node = new ArrayList<ChanceNode>();
		final BattleState state = s.clone();
		defend(state.clone(c));
		final String message = c.toString() + " defends...";
		node.add(new ChanceNode(state, 1f, message, Delay.WAIT));
		return node;
	}

	void defend(Combatant c) {
		c.ap += Defend.APCOST;
		if (!c.burrowed) {
			c.addcondition(new Defending(c.ap, c));
		}
	}
}
