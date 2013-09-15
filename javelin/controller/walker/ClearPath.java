package javelin.controller.walker;

import java.awt.Point;

import javelin.model.state.BattleState;
import javelin.model.state.Square;

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