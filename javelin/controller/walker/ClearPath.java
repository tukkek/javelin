package javelin.controller.walker;

import javelin.controller.Point;
import javelin.controller.walker.pathing.Pathing;
import javelin.model.state.BattleState;
import javelin.model.state.Square;

/**
 * Finds an unobstructed path.
 * 
 * @author alex
 */
public class ClearPath extends Walker {
	public ClearPath(Point from, Point to, BattleState s) {
		super(from, to, s);
	}

	public ClearPath(Point from, Point to, Pathing path, BattleState s) {
		super(from, to, path, s);
	}

	@Override
	protected boolean valid(int x, int y, BattleState state) {
		try {
			final Square square = state.map[x][y];
			return !square.blocked && !square.obstructed
					&& state.getcombatant(x, y) == null;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}
}