package javelin.model.world.location.dungeon.crawler;

import java.util.LinkedList;
import java.util.Set;

import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;
import tyrant.mikera.engine.RPG;

/**
 * Carves a rectangular area in a {@link Dungeon}.
 * 
 * @author alex
 */
public class Room extends Crawler {
	int lengthy;
	LinkedList<Point> room = new LinkedList<Point>();

	/** Constructor. */
	public Room(Point start, Set<Point> used) {
		super(start, used, RPG.r(3, 5));
		lengthy = RPG.r(3, 5);
		for (int x = walker.x - length / 2; x <= walker.x + length / 2; x++) {
			for (int y = walker.y - lengthy / 2; y <= walker.y
					+ lengthy / 2; y++) {
				room.add(new Point(x, y));
			}
		}
	}

	@Override
	public void step() {
		walker = room.pop();
	}
}