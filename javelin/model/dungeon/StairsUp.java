package javelin.model.dungeon;

import javelin.controller.Point;
import javelin.model.world.place.Dungeon;

/**
 * Leaves dungeon.
 * 
 * @author alex
 */
public class StairsUp extends Feature {

	public StairsUp(String thing, Point p) {
		super(thing, p.x, p.y);
	}

	@Override
	public void activate() {
		Dungeon.active.leave();
	}
}
