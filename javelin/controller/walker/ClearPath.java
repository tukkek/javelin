package javelin.controller.walker;

import javelin.controller.Point;
import javelin.model.state.BattleState;
import javelin.model.state.Square;

/**
 * Finds an unobstructed path.
 * 
 * @author alex
 */
public class ClearPath extends Walker {
	public ClearPath(Point me, Point target, BattleState state) {
		super(me, target, state);
	}

	@Override
	protected boolean valid(int x, int y, BattleState state) {
		try {
			final Square square = state.map[x][y];
			return !square.blocked && !square.obstructed
					&& state.getCombatant(x, y) == null;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}
}