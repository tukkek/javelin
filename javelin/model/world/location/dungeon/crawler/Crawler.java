package javelin.model.world.location.dungeon.crawler;

import java.util.ArrayList;
import java.util.Set;

import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Helper class to carve out paths and sections in a {@link Dungeon}.
 * 
 * @author alex
 */
public abstract class Crawler {
	/** Points of wall that have been carved out during crawling. */
	public ArrayList<Point> result = new ArrayList<Point>();
	/**
	 * Subclasses should increment this with each {@link #step()} so that the
	 * crawler can "walk" as it carves the {@link Dungeon}.
	 */
	protected Point walker;
	private Set<Point> used;

	/**
	 * @param start
	 *            Uses this as a starting point.
	 * @param used
	 *            Will not override any point here.
	 */
	public Crawler(Point start, Set<Point> used) {
		this.walker = start;
		this.used = used;
	}

	void crawl(int length) {
		for (int i = 0; i < length; i++) {
			step();
			if (!Dungeon.valid(walker.x) || !Dungeon.valid(walker.y)
					|| used.contains(walker)) {
				result = null;
				return;
			}
			result.add(new Point(walker.x, walker.y));
		}
	}

	/** Subclasses should use this to carve the {@link Dungeon}. */
	protected abstract void step();

	/**
	 * @param length
	 *            Number of times to call {@link #step()}.
	 * @param free
	 *            Will fill those with the {@link #result}.
	 */
	public void fill(int length, Set<Point> free) {
		crawl(length);
		if (result != null) {
			free.addAll(result);
		}
	}
}
