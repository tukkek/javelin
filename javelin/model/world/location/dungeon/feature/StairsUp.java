package javelin.model.world.location.dungeon.feature;

import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Leaves dungeon or goes to upper floor.
 *
 * @author alex
 */
public class StairsUp extends Feature {
	/** Cosntructor. */
	public StairsUp(String thing, Point p) {
		super(p.x, p.y, "dungeonstairsup");
		remove = false;
		enter = false;
	}

	@Override
	public boolean activate() {
		Dungeon.active.goup();
		return false;
	}
}
