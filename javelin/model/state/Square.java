package javelin.model.state;

import javelin.controller.terrain.map.Map;

/**
 * A singular area of a {@link Map}.
 * 
 * @author alex
 */
public class Square {
	public boolean blocked;
	public boolean obstructed;
	public boolean flooded;

	public Square(final boolean blocked, final boolean obstructed,
			boolean floodedp) {
		super();
		this.blocked = blocked;
		this.obstructed = obstructed;
		flooded = floodedp;
	}

	@Override
	public String toString() {
		if (blocked) {
			return "#";
		}
		if (obstructed) {
			return "-";
		}
		if (flooded) {
			return "~";
		}
		return " ";
	}
}