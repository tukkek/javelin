package javelin.model.state;

public class Square {
	public final boolean blocked;
	public final boolean obstructed;
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