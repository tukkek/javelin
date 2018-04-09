package javelin.controller.walker.pathing;

import java.util.ArrayList;

import javelin.controller.walker.Step;
import javelin.controller.walker.Walker;
import javelin.view.mappanel.MoveOverlay;

/**
 * TODO This probably can be overloaded by {@link MoveOverlay} to take more
 * steps and try to find the best solution instead of the fastest one.
 */
public class BranchingPath implements Pathing {
	public static final BranchingPath INSTANCE = new BranchingPath();

	private BranchingPath() {
		// prevent instantiation
	}

	@Override
	public ArrayList<Step> step(int x, int y, ArrayList<Step> steps, Walker w) {
		final int stepx = x + (w.tox > x ? +1 : -1);
		final int stepy = y + (w.toy > y ? +1 : -1);
		if (y != w.toy) {
			steps.add(new Step(x, stepy));
		}
		if (x != w.tox) {
			steps.add(new Step(stepx, y));
			if (y != w.toy) {
				steps.add(new Step(stepx, stepy));
			}
		}
		return steps;
	}
}