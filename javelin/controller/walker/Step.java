package javelin.controller.walker;

import javelin.controller.Point;

/**
 * Lightweight {@link Point}.
 * 
 * @author alex
 */
public class Step {
	public int x, y;

	public Step(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return x + "," + y;
	}
}