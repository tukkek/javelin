package javelin.model.world.location.dungeon.temple.features;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.old.Game;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.Trap;
import javelin.model.world.location.dungeon.temple.FireTemple;

/**
 * @see FireTemple
 * @author alex
 */
public class Brazier extends Feature {
	/** Constructor. */
	public Brazier(int xp, int yp) {
		super("dog", xp, yp, "dungeonbrazier");
	}

	@Override
	public boolean activate() {
		Javelin.message("You light up the brazier!", false);
		brighten(x, y, 0);
		JavelinApp.context.view(Game.hero());
		return true;
	}

	void brighten(int xp, int yp, int depth) {
		for (Feature f : Dungeon.active.features) {
			if (f.x != xp || f.y != xp) {
				continue;
			}
			Trap t = f instanceof Trap ? (Trap) f : null;
			if (t != null && !t.draw) {
				t.discover();
			}
		}
		try {
			Dungeon.active.setvisible(xp, yp);
		} catch (IndexOutOfBoundsException e) {
			return;
		}
		if (depth > 2) {
			return;
		}
		if (Dungeon.active.walls.contains(new Point(xp, yp))) {
			return;
		}
		for (int x = xp - 1; x <= xp + 1; x++) {
			for (int y = yp - 1; y <= yp + 1; y++) {
				if (x == xp && y == yp) {
					continue;
				}
				brighten(x, y, depth + 1);
			}
		}
	}
}
