package javelin.controller.action.area;

import java.util.Set;

import javelin.controller.Point;
import javelin.controller.action.Breath;
import javelin.controller.action.Fire;
import javelin.model.state.BattleState;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;

/**
 * Controller class to fill a certain region of the map by shape and a source
 * point.
 * 
 * TODO instead of taking an initial direction make {@link Breath} a
 * {@link Fire} and take a {@link Combatant} as `target`. This would be faster
 * since currently this requires 8 area calculations per turn and it would also
 * allow greater flexibility. For example: {@link Line} would be able to hit any
 * monster in sight and range not only those aligned horizontally, vertically or
 * diagonally. This would also be an interesting way to be able to cast Area
 * spells.
 * 
 * @author alex
 */
public abstract class Area {
	/**
	 * Relative initial square of the area in relation to the source
	 * {@link Combatant}.
	 */
	protected final Point direction;

	/**
	 * @see #direction
	 */
	public Area(final int sourcex, final int sourcey) {
		direction = new Point(sourcex, sourcey);
	}

	/**
	 * @param range
	 *            Dimension of area in feet (5 feet equals 1 square).
	 * @return Points that area filled by this area.
	 */
	abstract public Set<Point> fill(final int range, Combatant active,
			BattleState state);

	/**
	 * @return
	 */
	public Point initiate(Combatant m) {
		return new Point(m.location[0] + direction.x,
				m.location[1] + direction.y);
	}

	/**
	 * Used to also check for {@link Square#obstructed} but it wasn't much fun.
	 */
	static public boolean checkclear(final BattleState state, final Point p) {
		return p.x >= 0 && p.y >= 0 && p.x < state.map.length
				&& p.y < state.map[0].length && !state.map[p.x][p.y].blocked;
	}
}