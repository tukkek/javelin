/**
 * 
 */
package javelin.controller;

import java.io.Serializable;
import java.util.Objects;

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
}