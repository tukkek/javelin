/**
 * 
 */
package javelin.controller;

import java.io.Serializable;

/**
 * X Y coordinate.
 * 
 * @author alex
 */
public class Point implements Cloneable, Serializable {
	public int x;
	public int y;

	public Point(final int i, final int j) {
		x = i;
		y = j;
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
		return toString().hashCode();
	}
}