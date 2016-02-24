package javelin.controller.walker;

import javelin.controller.Point;
import javelin.controller.action.Charge;
import javelin.model.state.BattleState;

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
	protected boolean valid(int x, int y, BattleState state) {
		return (swimmer || !state.map[x][y].flooded)
				&& super.valid(x, y, state);
	}
}