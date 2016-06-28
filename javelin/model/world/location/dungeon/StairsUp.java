package javelin.model.world.location.dungeon;

import javelin.controller.Point;

/**
 * Leaves dungeon.
 * 
 * @author alex
 */
public class StairsUp extends Feature {

	public StairsUp(String thing, Point p) {
		super(thing, p.x, p.y, "dungeonstairs");
	}

	@Override
	public boolean activate() {
		Dungeon.active.leave();
		return true;
	}
}
