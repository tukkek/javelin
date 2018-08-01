package javelin.controller.walker.state;

import javelin.controller.Point;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;

public class StateWalker extends Walker {
	protected BattleState state;

	public StateWalker(Point from, Point to, BattleState s) {
		super(from, to);
		this.state = s;
	}

}