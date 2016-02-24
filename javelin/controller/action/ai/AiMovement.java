package javelin.controller.action.ai;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.Action;
import javelin.controller.action.Movement;
import javelin.controller.action.Wait;
import javelin.controller.ai.ActionProvider;
import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * @author alex
 * @see Movement
 */
public class AiMovement extends Action implements AiAction {
	public static final AiMovement SINGLETON = new AiMovement();
	static final Movement[][] movementgridbyy = new Movement[3][3];

	static {
		/* Tyrant vertical axis is inverted :P */
		movementgridbyy[0][0] = Action.MOVE_NW;
		movementgridbyy[0][1] = Action.MOVE_N;
		movementgridbyy[0][2] = Action.MOVE_NE;
		movementgridbyy[1][0] = Action.MOVE_W;
		movementgridbyy[1][2] = Action.MOVE_E;
		movementgridbyy[2][0] = Action.MOVE_SW;
		movementgridbyy[2][1] = Action.MOVE_S;
		movementgridbyy[2][2] = Action.MOVE_SE;
	}

	private AiMovement() {
		super("Move");
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(final BattleState gameStatep,
			final Combatant active) {
		final ArrayList<List<ChanceNode>> successors =
				new ArrayList<List<ChanceNode>>();
		for (int x = active.location[0] - 1, deltax =
				-1; deltax <= +1; x++, deltax++) {
			movement: for (int y = active.location[1] - 1, deltay =
					-1; deltay <= +1; y++, deltay++) {
				if (deltax == 0 && deltay == 0) {
					continue;
				}
				final BattleState gameState = gameStatep;
				if (x < 0 || y < 0 || x >= gameState.map.length
						|| y >= gameState.map[0].length) {
					continue;
				}
				if (gameState.getCombatant(x, y) != null
						|| gameState.map[x][y].blocked
								&& active.source.fly == 0) {
					continue;
				}
				Meld meld = null;
				for (Meld m : gameState.meld) {
					if (m.x == x && m.y == y) {
						if (!m.crystalize(gameState)) {
							continue movement;
						}
						meld = m;
						break;
					}
				}
				successors.add(registermove(deltax, deltay, active, gameState,
						x, y, meld));
			}
		}
		if (!Wait.ALLOWAI && successors.isEmpty()) {
			successors.add(wait(gameStatep, active));
		}
		return successors;
	}

	static private ArrayList<ChanceNode> registermove(final int deltax,
			final int deltay, Combatant active, BattleState gameState,
			final int x, final int y, Meld meld) {
		gameState = gameState.clone();
		active = gameState.clone(active);
		final Movement moveaction = movementgridbyy[deltay + 1][deltax + 1];
		final boolean disengaging = moveaction.isDisengaging(active, gameState);
		active.ap += moveaction.cost(active, gameState, x, y);
		active.location[0] = x;
		active.location[1] = y;
		final ArrayList<ChanceNode> list = new ArrayList<ChanceNode>(1);
		String action;
		Delay delay;
		if (meld == null) {
			action = active + (disengaging ? " disengages..." : " moves...");
			delay = Delay.WAIT;
		} else {
			action = active + " powers up!";
			delay = Delay.BLOCK;
			active.meld();
			gameState.meld.remove(meld);
		}
		list.add(new ChanceNode(gameState, 1f, action, delay));
		if (Javelin.DEBUG) {
			// TODO debug
			ActionProvider.checkstacking(gameState);
		}
		return list;
	}

	static public ArrayList<ChanceNode> wait(final BattleState gameState,
			final Combatant active) {
		final ArrayList<ChanceNode> wait = new ArrayList<ChanceNode>();
		final BattleState state = gameState.clone();
		state.clone(active).await();
		wait.add(new ChanceNode(state, 1f, active.toString() + " defends...",
				Delay.WAIT));
		return wait;
	}
}