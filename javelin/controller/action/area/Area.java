package javelin.controller.action.area;

import java.util.Set;

import javelin.controller.Point;
import javelin.model.state.BattleState;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;

public abstract class Area {
	protected final Point source;

	public Area(final int sourcex, final int sourcey) {
		source = new Point(sourcex, sourcey);
	}

	abstract public Set<Point> fill(final int range, Combatant c,
			BattleState state);

	public Point pointsource(Combatant m) {
		return new Point(m.location[0] + source.x, m.location[1] + source.y);
	}

	public boolean checkclear(final BattleState state, final Point p) {
		final Square s = state.map[p.x][p.y];
		boolean checkclear = !s.blocked && !s.obstructed;
		return checkclear;
	}
}