package javelin.model.world.location.dungeon.crawler;

import java.util.LinkedList;
import java.util.Set;

import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Carves a rectangular area in a {@link Dungeon}.
 * 
 * @author alex
 */
public class Room extends Crawler {
	final LinkedList<Point> room = new LinkedList<Point>();

	/** Constructor. */
	public Room(Point start, Set<Point> used) {
		super(start, used);
	}

	@Override
	public void fill(int length, Set<Point> free) {
		for (int x = walker.x - length; x <= walker.x + length; x++) {
			for (int y = walker.y - length; y <= walker.y + length; y++) {
				room.add(new Point(x, y));
			}
		}
		super.fill(length, free);
	}

	@Override
	public void step() {
		Point p = room.pop();
		walker.x = p.x;
		walker.y = p.y;
	}
}