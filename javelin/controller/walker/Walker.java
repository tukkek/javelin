package javelin.controller.walker;

import java.util.ArrayList;

import javelin.controller.Point;
import javelin.controller.walker.pathing.BranchingPath;
import javelin.controller.walker.pathing.Pathing;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * Extensible path-finding algorithm.
 * 
 * TODO improve documentation for all hierarchy.
 * 
 * @author alex
 */
public class Walker {
	public static final int[] DELTAS = new int[] { 0, +1, -1 };
	public ArrayList<Step> solution = null;
	public final BattleState state;
	public int fromx;
	public int fromy;
	public int tox;
	public int toy;
	public ArrayList<Step> partial = null;
	public Pathing pathing = BranchingPath.INSTANCE;

	public Walker(Point from, Point to, BattleState s) {
		this.state = s;
		fromx = from.x;
		fromy = from.y;
		tox = to.x;
		toy = to.y;
	}

	public Walker(Point from, Point to, Pathing p, BattleState s) {
		this(from, to, s);
		pathing = p;
	}

	public ArrayList<Step> walk() {
		walk(fromx, fromy, new ArrayList<Step>());
		return solution;
	}

	private void walk(int x, int y, ArrayList<Step> steps) {
		if (solution != null && steps.size() >= solution.size()) {
			return;
		}
		if (x == tox && y == toy) {
			solution = steps;
			return;
		}
		if (!steps.isEmpty() && !valid(x, y)) {
			steps.remove(steps.size() - 1);
			partial = steps;
			return;
		}
		ArrayList<Step> nextsteps = pathing.step(x, y, getsteplist(), this);
		for (Step step : nextsteps) {
			final ArrayList<Step> stepinto = (ArrayList<Step>) steps.clone();
			if (step.x != tox || step.y != toy) {
				stepinto.add(step);
			}
			walk(step.x, step.y, stepinto);
		}
	}

	protected ArrayList<Step> getsteplist() {
		return new NextMove(tox, toy);
	}

	public boolean valid(int x, int y) {
		return true;
	}

	public void reset() {
		solution = null;
		partial = null;
	}

	public static int distanceinsteps(int ax, int ay, int bx, int by) {
		return Math.max(Math.abs(ax - bx), Math.abs(ay - by));
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
