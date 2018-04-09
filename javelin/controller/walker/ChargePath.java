package javelin.controller.walker;

import javelin.controller.Point;
import javelin.controller.action.Charge;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;

/**
 * @see Charge
 * 
 * @author alex
 */
public class ChargePath extends ClearPath {
	private final boolean swimmer;

	public ChargePath(Point me, Point target, BattleState state,
			boolean swimmerp) {
		super(me, target, state);
		swimmer = swimmerp;
	}

	@Override
	public boolean valid(int x, int y) {
		for (Meld m : state.meld) {
			if (m.x == x && m.y == y) {
				return false;
			}
		}
		return (swimmer || !state.map[x][y].flooded) && super.valid(x, y);
	}
}