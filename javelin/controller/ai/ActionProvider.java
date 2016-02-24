package javelin.controller.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javelin.Javelin;
import javelin.controller.action.Action;
import javelin.controller.action.ActionMapping;
import javelin.controller.action.ai.AiAction;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * An {@link Iterator} that uses {@link Action}s from
 * {@link ActionMapping#actions} and returns it's successors as
 * {@link ChanceNode}.
 * 
 * @see AiAction#getoutcomes(BattleState, Combatant)
 * 
 * @author alex
 */
public final class ActionProvider
		implements Iterable<List<ChanceNode>>, Iterator<List<ChanceNode>> {
	static final ArrayList<AiAction> ACTIONS = new ArrayList<AiAction>();

	static {
		for (Action a : ActionMapping.actions) {
			if (a instanceof AiAction) {
				ActionProvider.ACTIONS.add((AiAction) a);
			}
		}
	}

	static public void validate(final List<ChanceNode> newsucessors) {
		float sum = 0;
		for (final ChanceNode cn : newsucessors) {
			sum += cn.chance;
		}
		if (.95 >= sum || sum >= 1.05f) {
			throw new RuntimeException("Invalid chances");
		}
	}

	final BattleState battleState;
	final Stack<AiAction> actions = new Stack<AiAction>();

	public ActionProvider(BattleState battleState) {
		this.battleState = battleState;
		if (Javelin.DEBUG) {// TODO debug
			checkstacking(battleState);
		}
		actions.addAll((List<AiAction>) ActionProvider.ACTIONS.clone());
	}

	@Override
	public Iterator<List<ChanceNode>> iterator() {
		return this;
	}

	final Stack<List<ChanceNode>> queue = new Stack<List<ChanceNode>>();

	@Override
	public boolean hasNext() {
		return !actions.isEmpty() || !queue.isEmpty();
	}

	@Override
	public List<ChanceNode> next() {
		if (!queue.isEmpty()) {
			List<ChanceNode> n = queue.pop();
			return n;
		}
		if (actions.isEmpty()) {
			return Collections.emptyList();
		}
		if (Javelin.DEBUG) {
			checkstacking(battleState);// TODO debug
		}
		final BattleState stateclone = battleState.deepclone();
		final List<List<ChanceNode>> outcomes =
				actions.pop().getoutcomes(stateclone, stateclone.next);
		for (final List<ChanceNode> sucessors : outcomes) {
			if (!sucessors.isEmpty()) {
				if (Javelin.DEBUG) {
					ActionProvider.validate(sucessors);
					checkstacking(sucessors);// TODO debug
				}
				queue.add(sucessors);
			}
		}
		return next();
	}

	/**
	 * TODO debug
	 */
	static public void checkstacking(List<ChanceNode> n) {
		for (ChanceNode cn : n) {// TODO debug
			checkstacking((BattleState) cn.n);
		}
	}

	/**
	 * TODO debug
	 */
	public static void checkstacking(BattleState s) {
		HashSet<String> locations = new HashSet<String>();
		for (Combatant c : s.getCombatants()) {
			if (!locations.add(c.location[0] + ":" + c.location[1])) {
				System.out.println("Stacked combatants!");
			}
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}