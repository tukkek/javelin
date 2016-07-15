package javelin.model.world.location.dungeon.temple.features;

import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;

/**
 * Descends deeper into the dungeon.
 * 
 * @author alex
 */
public class StairsDown extends Feature {
	/** Constructor. */
	public StairsDown(String thing, Point p) {
		super(thing, p.x, p.y, "dungeonstairsdown");
		remove = false;
	}

	@Override
	public boolean activate() {
		Dungeon.active.godown();
		return false;
	}
}
