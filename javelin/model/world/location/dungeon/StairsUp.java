package javelin.model.world.location.dungeon;

import javelin.controller.Point;

/**
 * Leaves dungeon or goes to upper floor.
 * 
 * @author alex
 */
public class StairsUp extends Feature {
	/** Cosntructor. */
	public StairsUp(String thing, Point p) {
		super(thing, p.x, p.y, "dungeonstairsup");
		remove = false;
	}

	@Override
	public boolean activate() {
		Dungeon.active.goup();
		return false;
	}
}
