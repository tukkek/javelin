package javelin.controller.walker;

import java.util.ArrayList;

/**
 * Possible immediate movement options.
 * 
 * @author alex
 */
public class NextMove extends ArrayList<Step> {

	private final int targetx;
	private final int targety;
	private double currentsourcedistance = Integer.MAX_VALUE;
	private double currenttargetdistance = Integer.MAX_VALUE;

	public NextMove(int targetx, int targety) {
		super(3);
		this.targetx = targetx;
		this.targety = targety;
	}

	@Override
	public boolean add(Step step) {
		if (isEmpty()) {
			return super.add(step);
		}
		final double newtargetdistance =
				distance(targetx, targety, step.x, step.y);
		if (newtargetdistance > currenttargetdistance) {
			return false;
		}
		if (newtargetdistance < currenttargetdistance) {
			clear();
			currentsourcedistance = newtargetdistance;
		}
		return super.add(step);
	}

	static private double distance(int ax, int ay, int bx, int by) {
		// return Math.min(Math.abs(ax - bx), Math.abs(ay - by));
		return Walker.distance(ax, ay, bx, by);
	}
}
