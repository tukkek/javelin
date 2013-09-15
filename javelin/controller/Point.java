/**
 * 
 */
package javelin.controller;

import java.io.Serializable;

public class Point implements Cloneable, Serializable {
	public final int x;
	public final int y;

	public Point(final int i, final int j) {
		x = i;
		y = j;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Point) {
			final Point p = (Point) obj;
			return p.x == x && p.y == y;
		}
		return false;
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