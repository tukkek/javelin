package javelin.controller.action.area;

import java.util.HashSet;
import java.util.Set;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Combatant;

public class Line extends Area {

	public Line(final int sourcex, final int sourcey) {
		super(sourcex, sourcey);
	}

	@Override
	public Set<Point> fill(int range, Combatant m, BattleState state) {
		final HashSet<Point> area = new HashSet<Point>();
		final Point sourcepoint = pointsource(m);
		if (!checkclear(state, sourcepoint)) {
			area.add(sourcepoint);
			return area;
		}
		range = (range - 5) / 5;
		Point p = sourcepoint;
		while (Walker.distance(p.x, p.y, sourcepoint.x, sourcepoint.y) <= range
				&& state.hasLineOfSight(new Point(sourcepoint.x, sourcepoint.y),
						new Point(p.x, p.y), range,
						Javelin.PERIOD_NOON) == Vision.CLEAR) {
			area.add(p);
			p = new Point(p.x + source.x, p.y + source.y);
		}
		return area;
	}
}
