package javelin.model.world.location.dungeon;

import java.util.ArrayList;
import java.util.Set;

import javelin.controller.Point;

/**
 * Helper class to carve out paths and sections in a {@link Dungeon}.
 * 
 * @author alex
 */
public abstract class Crawler {
	protected Point walker;
	public ArrayList<Point> result = new ArrayList<Point>();
	private Set<Point> used;

	public Crawler(Point start, Set<Point> used) {
		this.walker = start;
		this.used = used;
	}

	public void crawl(int length) {
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

	public abstract void step();

	public void fill(int length, Set<Point> free) {
		crawl(length);
		if (result != null) {
			free.addAll(result);
		}
	}
}
