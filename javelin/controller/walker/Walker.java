package javelin.controller.walker;

import java.util.ArrayList;

import javelin.controller.Point;
import javelin.controller.action.Target;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.battle.overlay.MoveOverlay;

/**
 * Extensible path-finding algorithm.
 * 
 * @author alex
 */
public class Walker {
	public static final int[] DELTAS = new int[] { 0, +1, -1 };
	protected int targetx;
	protected int targety;
	public ArrayList<Step> solution = null;
	protected final BattleState state;
	private int sourcex;
	private int sourcey;
	public ArrayList<Step> partial;

	public Walker(Point me, Point target, BattleState state) {
		this.state = state;
		targetx = target.x;
		targety = target.y;
		sourcex = me.x;
		sourcey = me.y;
	}

	public ArrayList<Step> walk() {
		walk(sourcex, sourcey, new ArrayList<Step>());
		return solution;
	}

	private void walk(int x, int y, ArrayList<Step> steps) {
		if (solution != null && steps.size() >= solution.size()) {
			return;
		}
		if (x == targetx && y == targety) {
			solution = steps;
			return;
		}
		if (!steps.isEmpty() && !valid(x, y, state)) {
			if (!steps.isEmpty()) {
				steps.remove(steps.size() - 1);
			}
			partial = steps;
			return;
		}
		ArrayList<Step> nextsteps = takebeststep(x, y);
		for (Step step : nextsteps) {
			final ArrayList<Step> stepinto = (ArrayList<Step>) steps.clone();
			if (step.x != targetx || step.y != targety) {
				stepinto.add(step);
			}
			walk(step.x, step.y, stepinto);
		}
	}

	/**
	 * TODO This probably can be overloaded by {@link MoveOverlay} to take more
	 * steps and try to find the best solution instead of the fastest one.
	 * 
	 * TODO this produces weird, almost curved angles for {@link Target} since
	 * it goes ↘__
	 */
	protected ArrayList<Step> takebeststep(final int x, final int y) {
		final ArrayList<Step> steps = getsteplist();
		final int stepx = x + (targetx > x ? +1 : -1);
		final int stepy = y + (targety > y ? +1 : -1);
		if (y != targety) {
			steps.add(new Step(x, stepy));
		}
		if (x != targetx) {
			steps.add(new Step(stepx, y));
			if (y != targety) {
				steps.add(new Step(stepx, stepy));
			}
		}
		return steps;
	}

	protected ArrayList<Step> getsteplist() {
		return new NextMove(targetx, targety);
	}

	protected boolean valid(int x, int y, BattleState state2) {
		return true;
	}

	public static double distance(final Combatant c1, final Combatant c2) {
		return distance(c1.location[0], c1.location[1], c2.location[0],
				c2.location[1]);
	}

	public static double distance(final int ax, final int ay, final int bx,
			final int by) {
		final int deltax = Math.abs(ax - bx);
		final int deltay = Math.abs(ay - by);
		return Math.sqrt(deltax * deltax + deltay * deltay);
	}
}
