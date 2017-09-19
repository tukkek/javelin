package javelin.controller.walker;

import javelin.controller.Point;

/**
 * Lightweight {@link Point}.
 * 
 * @author alex
 */
public class Step extends Point {
	public Step(int x, int y) {
		super(x, y);
	}

	@Override
	public String toString() {
		return x + "," + y;
	}
}