package javelin.model.world.location.dungeon.feature;

import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Descends deeper into the dungeon.
 *
 * @author alex
 */
public class StairsDown extends Feature {
	/** Constructor. */
	public StairsDown(Point p) {
		super(p.x, p.y, "dungeonstairsdown");
		remove = false;
	}

	@Override
	public boolean activate() {
		Dungeon.active.godown();
		return false;
	}
}
