/**
 * 
 */
package javelin.controller;

import java.io.Serializable;
import java.util.Objects;

import javelin.model.unit.attack.Combatant;

/**
 * X Y coordinate.
 * 
 * @author alex
 */
public class Point implements Cloneable, Serializable {
	public int x;
	public int y;

	public Point(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public Point(Point p) {
		this(p.x, p.y);
	}

	public Point(Combatant c) {
		x = c.location[0];
		y = c.location[1];
	}

	@Override
	public boolean equals(final Object obj) {
		final Point p = (Point) obj;
		return p.x == x && p.y == y;
	}

	@Override
	public String toString() {
		return x + ":" + y;
	}

	@Override
	public Point clone() {
		try {
			return (Point) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	/**
	 * @return <code>true</code> if this is valid inside the bounds of a
	 *         2-dimensional array (not that max values are exclusive).
	 */
	public boolean validate(int minx, int miny, int maxx, int maxy) {
		return minx <= x && x < maxx && miny <= y && y < maxy;
	}
}