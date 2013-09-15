package javelin.controller.action.area;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Combatant;

public class Burst extends Area {
	final ArrayList<Point> directions = new ArrayList<Point>();

	public Burst(final int sourcex, final int sourcey, final int ax,
			final int ay, final int bx, final int by, final int cx, final int cy) {
		super(sourcex, sourcey);
		directions.add(new Point(ax, ay));
		directions.add(new Point(bx, by));
		directions.add(new Point(cx, cy));
	}

	@Override
	public Set<Point> fill(int range, final Combatant c, final BattleState state) {
		range -= 5;
		final HashSet<Point> area = new HashSet<Point>();
		final Point p = pointsource(c);
		if (checkclear(state, p)) {
			recursivefill(p, p, range, area, state);
		}
		return area;
	}

	private void recursivefill(final Point source, final Point current,
			final int range, final HashSet<Point> area, final BattleState s) {
		if (Walker.distance(source.x, source.y, current.x, current.y) > range / 5) {
			return;
		}
		if (s.hasLineOfSight(new java.awt.Point(source.x, source.y),
				new java.awt.Point(current.x, current.y), range / 5,
				Javelin.PERIOD_NOON) != Vision.CLEAR) {
			return;
		}
		area.add(current);
		for (final Point direction : directions) {
			recursivefill(source, new Point(current.x + direction.x, current.y
					+ direction.y), range, area, s);
		}
	}
}