package javelin.controller.walker;

import java.awt.Point;
import java.util.ArrayList;

import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class Walker {
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

	public static final int[] DELTAS = new int[] { 0, +1, -1 };
	int targetx;
	int targety;
	public ArrayList<Step> solution = null;
	private final BattleState state;
	private int sourcex;
	private int sourcey;

	public Walker(Point me, Point target, BattleState state) {
		this.state = state;
		targetx = target.x;
		targety = target.y;
		sourcex = me.x;
		sourcey = me.y;
	}

	public ArrayList<Step> walk() {
		walk(sourcex, sourcey, new ArrayList<Walker.Step>());
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
			return;
		}
		Step beststep = null;
		for (int deltax : DELTAS) {
			for (int deltay : DELTAS) {
				final Step step = new Step(x + deltax, y + deltay);
				if (beststep == null
						|| distance(step.x, step.y, targetx, targety) < distance(
								beststep.x, beststep.y, targetx, targety)) {
					beststep = step;
				}
			}
		}
		final ArrayList<Step> stepinto = (ArrayList<Step>) steps.clone();
		if (beststep.x != targetx || beststep.y != targety) {
			stepinto.add(beststep);
		}
		walk(beststep.x, beststep.y, stepinto);
	}

	protected boolean valid(int x, int y, BattleState state2) {
		return true;
	}

	public static double distance(Combatant c1, Combatant c2) {
		return distance(c1.location[0], c1.location[1], c2.location[0], c2.location[1]);
	}

	public static double distance(int ax, int ay, int bx, int by) {
		final int deltax = Math.abs(ax - bx);
		final int deltay = Math.abs(ay - by);
		return Math.sqrt(deltax * deltax + deltay * deltay);
	}
}
