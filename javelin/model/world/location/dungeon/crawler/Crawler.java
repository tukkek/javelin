package javelin.model.world.location.dungeon.crawler;

import java.util.ArrayList;
import java.util.Set;

import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;
import tyrant.mikera.engine.RPG;

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
	Set<Point> used;
	int length;

	/**
	 * @param start
	 *            Uses this as a starting point.
	 * @param used
	 *            Will not override any point here.
	 * @param lengthp
	 * @param length
	 * @param length
	 */
	public Crawler(Point start, Set<Point> used, int lengthp) {
		this.walker = start.clone();
		this.used = used;
		length = lengthp;
	}

	void crawl() {
		result.add(new Point(walker.x, walker.y));
		for (int i = 0; i < length; i++) {
			step();
			if (!validate()) {
				result = null;
				return;
			}
			result.add(new Point(walker.x, walker.y));
		}
	}

	public boolean validate() {
		return Dungeon.valid(walker.x) && Dungeon.valid(walker.y)
				&& !used.contains(walker);
	}

	/** Subclasses should use this to carve the {@link Dungeon}. */
	protected abstract void step();

	/**
	 * @param length
	 *            Number of times to call {@link #step()}.
	 * @param free
	 *            Will fill those with the {@link #result}.
	 */
	public void fill(Set<Point> free) {
		crawl();
		if (result != null) {
			free.addAll(result);
		}
	}

	public static void carve(Point current, Set<Point> free, Set<Point> used) {
		boolean horizontal = RPG.r(1, 2) == 1;
		while (free.size() < Dungeon.WALKABLEAREA) {
			horizontal = !horizontal;
			Crawler c;
			if (RPG.r(1, 10) == 1) {
				c = new Room(current, used);
			} else if (RPG.r(1, 2) == 1) {
				c = new HorizontalCorridor(current, used);
			} else {
				c = new VerticalCorridor(current, used);
			}
			c.fill(free);
			if (c.result != null) {
				current = RPG.pick(c.result);
			}
		}
	}
}
