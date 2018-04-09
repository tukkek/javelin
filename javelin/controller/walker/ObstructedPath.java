package javelin.controller.walker;

import javelin.controller.Point;
import javelin.controller.walker.pathing.Pathing;
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

	public ObstructedPath(Point from, Point to, Pathing path, BattleState s) {
		super(from, to, path, s);
	}

	@Override
	public boolean valid(int x, int y) {
		try {
			return !state.map[x][y].blocked;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}
}