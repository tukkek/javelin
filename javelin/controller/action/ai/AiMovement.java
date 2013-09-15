package javelin.controller.action.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javelin.controller.action.Action;
import javelin.controller.action.Movement;
import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import tyrant.mikera.tyrant.Game.Delay;

public class AiMovement extends Action {
	public static final AiMovement SINGLETON = new AiMovement();
	static final TreeMap<Integer, TreeMap<Integer, Movement>> movementgridbyy = new TreeMap<Integer, TreeMap<Integer, Movement>>();

	static {
		final TreeMap<Integer, Movement> toprow = new TreeMap<Integer, Movement>();
		final TreeMap<Integer, Movement> midrow = new TreeMap<Integer, Movement>();
		final TreeMap<Integer, Movement> bottomrow = new TreeMap<Integer, Movement>();
		/* Tyrant vertical axis is inverted :P */
		toprow.put(-1, Action.MOVE_NW);
		toprow.put(0, Action.MOVE_N);
		toprow.put(+1, Action.MOVE_NE);
		midrow.put(-1, Action.MOVE_W);
		midrow.put(+1, Action.MOVE_E);
		bottomrow.put(-1, Action.MOVE_SW);
		bottomrow.put(0, Action.MOVE_S);
		bottomrow.put(+1, Action.MOVE_SE);
		movementgridbyy.put(-1, toprow);
		movementgridbyy.put(0, midrow);
		movementgridbyy.put(+1, bottomrow);
	}

	private AiMovement() {
		super("Move");
	}

	@Override
	public List<List<ChanceNode>> getSucessors(final BattleState gameState,
			final Combatant active) {
		final ArrayList<List<ChanceNode>> successors = new ArrayList<List<ChanceNode>>();
		for (int x = active.location[0] - 1, deltax = -1; x <= active.location[0] + 1; x++, deltax++) {
			for (int y = active.location[1] - 1, deltay = -1; y <= active.location[1] + 1; y++, deltay++) {
				if (deltax == 0 && deltay == 0) {
					continue;
				}
				if (x < 0 || y < 0 || x >= gameState.map.length
						|| y >= gameState.map[0].length) {
					continue;
				}
				if (gameState.getCombatant(x, y) != null
						|| gameState.map[x][y].blocked
						&& active.source.fly == 0) {
					continue;
				}
				successors.add(registermove(deltax, deltay, active, gameState,
						x, y));
			}
		}
		if (successors.isEmpty()) {
			successors.add(wait(gameState, active));
		}
		return successors;
	}

	static private ArrayList<ChanceNode> registermove(final int deltax,
			final int deltay, final Combatant active,
			final BattleState gameState, final int x, final int y) {
		final BattleState battleState = gameState.clone();
		final Combatant active2 = battleState.translatecombatant(active);
		final Movement moveaction = movementgridbyy.get(deltay).get(deltax);
		final boolean disengaging = moveaction.isDisengaging(active2,
				battleState);
		active2.ap += moveaction.cost(active2, battleState, x, y);
		active2.location[0] = x;
		active2.location[1] = y;
		final ArrayList<ChanceNode> list = new ArrayList<ChanceNode>();
		list.add(new ChanceNode(battleState, 1f, active
				+ (disengaging ? " disengages..." : " moves..."), Delay.WAIT));
		return list;
	}

	static public ArrayList<ChanceNode> wait(final BattleState gameState,
			final Combatant active) {
		final ArrayList<ChanceNode> wait = new ArrayList<ChanceNode>();
		final BattleState state = gameState.clone();
		state.translatecombatant(active).await();
		wait.add(new ChanceNode(state, 1f, active.toString() + " waits...",
				Delay.WAIT));
		return wait;
	}
}