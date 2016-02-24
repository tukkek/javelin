package javelin.controller.walker;

import javelin.controller.Point;
import javelin.model.state.BattleState;

/**
 * Finds any path, clear or obstructed but not blocked.
 * 
 * @author alex
 */
public class ObstructedPath extends Walker {
	public ObstructedPath(Point me, Point target, BattleState state) {
		super(me, target, state);
	}

	@Override
	protected boolean valid(int x, int y, BattleState state) {
		try {
			return !state.map[x][y].blocked;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}
}