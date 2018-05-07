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
	static final int RADIUS = 13;

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
		try {
			Dungeon.active.setvisible(p.x, p.y);
		} catch (IndexOutOfBoundsException e) {
			return;
		}
		Feature f = Dungeon.active.getfeature(p.x, p.y);
		if (f != null) {
			f.discover(null, 9000);
		}
		if (depth > RADIUS || Dungeon.active.map[p.x][p.y] == Template.WALL
				|| f instanceof Door) {
			return;
		}
		for (Point adjacent : Point.getadjacent()) {
			adjacent.x += p.x;
			adjacent.y += p.y;
			brighten(adjacent, depth + 1, visited);
		}
	}
}
