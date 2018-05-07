package javelin.controller.action.area;

import java.util.HashSet;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * Occupies a straight line.
 * 
 * @author alex
 */
public class Line extends Area {

	public Line(final int sourcex, final int sourcey) {
		super(sourcex, sourcey);
	}

	@Override
	public Set<Point> fill(int range, Combatant m, BattleState state) {
		final HashSet<Point> area = new HashSet<Point>();
		final Point sourcepoint = initiate(m);
		if (!checkclear(state, sourcepoint)) {
			area.add(sourcepoint);
			return area;
		}
		range = (range - 5) / 5;
		Point p = sourcepoint;
		while (Walker.distance(p.x, p.y, sourcepoint.x, sourcepoint.y) <= range
				&& checkclear(state, p)
		/*
		 * && state.hasLineOfSight(new Point(sourcepoint.x, sourcepoint.y), new
		 * Point(p.x, p.y), range, Javelin.PERIOD_NOON) == Vision.CLEAR
		 */) {
			area.add(p);
			p = new Point(p.x + direction.x, p.y + direction.y);
		}
		return area;
	}
}
