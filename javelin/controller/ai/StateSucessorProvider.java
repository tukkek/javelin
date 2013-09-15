package javelin.controller.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javelin.controller.action.Action;
import javelin.controller.action.ActionMapping;
import javelin.model.state.BattleState;

public final class StateSucessorProvider implements Iterable<List<ChanceNode>> {
	private final class StateIterator implements Iterator<List<ChanceNode>> {
		Stack<List<ChanceNode>> queue = new Stack<List<ChanceNode>>();

		@Override
		public boolean hasNext() {
			return !actions.isEmpty() || !queue.isEmpty();
		}

		@Override
		public List<ChanceNode> next() {
			if (!queue.isEmpty()) {
				return queue.pop();
			}
			if (actions.isEmpty()) {
				return Collections.emptyList();
			}
			battleState.checkwhoisnext();
			BattleState stateclone = battleState.clone();
			for (final List<ChanceNode> newsucessors : actions.pop()
					.getSucessors(stateclone,
							stateclone.translatecombatant(battleState.next))) {
				if (newsucessors.isEmpty()) {
					continue;
				}
				validate(newsucessors);
				queue.add(newsucessors);
			}
			return next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
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

	private final BattleState battleState;
	Stack<Action> actions = new Stack<Action>();
	static final List<Action> ACTIONS = Arrays.asList(ActionMapping.actions);

	public StateSucessorProvider(BattleState battleState) {
		this.battleState = battleState;
		List<Action> actionlist = new ArrayList<Action>(ACTIONS);
		Collections.shuffle(actionlist);
		for (final Action a : actionlist) {
			actions.push(a);
		}
	}

	@Override
	public Iterator<List<ChanceNode>> iterator() {
		return new StateIterator();
	}
}