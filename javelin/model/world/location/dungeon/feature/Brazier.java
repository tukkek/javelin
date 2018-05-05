package javelin.model.world.location.dungeon.feature;

import java.util.HashSet;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.model.world.location.dungeon.temple.FireTemple;

/**
 * @see FireTemple
 * @author alex
 */
public class Brazier extends Feature {
	/** Constructor. */
	public Brazier(int xp, int yp) {
		super(xp, yp, "dungeonbrazier");
	}

	@Override
	public boolean activate() {
		Javelin.message("You light up the brazier!", false);
		brighten(new Point(x, y), 0, new HashSet<Point>());
		Point p = JavelinApp.context.getherolocation();
		JavelinApp.context.view(p.x, p.y);
		return true;
	}

	/**
	 * TODO add a recursion cache (HashSet<Point>) to avoid taking too long.
	 */
	void brighten(Point p, int depth, HashSet<Point> visited) {
		if (!visited.add(p)) {
			return;
		}
		Feature f = Dungeon.active.getfeature(p.x, p.y);
		Trap t = f instanceof Trap ? (Trap) f : null;
		if (t != null && !t.draw) {
			t.discover();
		}
		try {
			Dungeon.active.setvisible(p.x, p.y);
		} catch (IndexOutOfBoundsException e) {
			return;
		}
		if (depth > 9 || Dungeon.active.map[p.x][p.y] == Template.WALL
				|| f instanceof Door) {
			return;
		}
		for (int x = p.x - 1; x <= p.x + 1; x++) {
			for (int y = p.y - 1; y <= p.y + 1; y++) {
				if (x == p.x && y == p.y) {
					continue;
				}
				brighten(new Point(x, y), depth + 1, visited);
			}
		}
	}
}
