package tyrant.mikera.tyrant;

import java.awt.event.KeyEvent;

import javelin.controller.Movement;
import javelin.controller.action.Action;
import javelin.controller.action.ActionDescription;
import javelin.controller.action.ActionMapping;
import javelin.model.state.BattleState;
import tyrant.mikera.engine.Point;

public class GameHandler {
	private ActionMapping actionMapping;

	public GameHandler() {
		actionMapping = new ActionMapping();
		actionMapping.addDefaultMappings();
		// actionMapping.addRougeLikeMappings();
	}

	public Point doDirection(final ActionDescription action,
			final BattleState state) {
		final Point direction = convertActionToDirection(action);
		final int tox = state.next.location[0] + direction.x;
		final int toy = state.next.location[1] + direction.y;
		if (Movement.tryMove(tox, toy, state)) {
			return new Point(tox, toy);
		}
		return null;
	}

	public Action actionFor(final KeyEvent keyEvent) {
		return actionMapping.actionFor(keyEvent);
	}

	public Point convertKeyToDirection(final char k) {
		final ActionDescription action = actionMapping.convertKeyToAction(k);
		return convertActionToDirection(action);
	}

	public static Point
			convertActionToDirection(final ActionDescription action) {
		final Point direction = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
		if (action == Action.MOVE_N) {
			direction.x = 0;
			direction.y = -1;
		}
		if (action == Action.MOVE_S) {
			direction.x = 0;
			direction.y = 1;
		}
		if (action == Action.MOVE_W) {
			direction.x = -1;
			direction.y = 0;
		}
		if (action == Action.MOVE_E) {
			direction.x = 1;
			direction.y = 0;
		}
		if (action == Action.MOVE_NW) {
			direction.x = -1;
			direction.y = -1;
		}
		if (action == Action.MOVE_NE) {
			direction.x = 1;
			direction.y = -1;
		}
		if (action == Action.MOVE_SW) {
			direction.x = -1;
			direction.y = 1;
		}
		if (action == Action.MOVE_SE) {
			direction.x = 1;
			direction.y = 1;
		}
		// if (action == Action.MOVE_NOWHERE) {
		// direction.x = 0;
		// direction.y = 0;
		// }
		return direction.x == Integer.MIN_VALUE ? null : direction;
	}

	public void setActionMapping(final ActionMapping actionMapping) {
		this.actionMapping = actionMapping;
	}
}
