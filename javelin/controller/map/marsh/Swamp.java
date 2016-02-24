package javelin.controller.map.marsh;

import javelin.controller.Weather;
import javelin.controller.map.DndMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.tyrant.Tile;

/**
 * @see DndMap
 */
public class Swamp extends DndMap {
	public Swamp() {
		super(.2, .2, .6);
		floor = Tile.GUNK;
		maxflooding = Weather.DRY;
	}

	@Override
	public void putwall(int x, int y) {
		map.addThing(Lib.create("tree"), x, y);
	}

	@Override
	public void putobstacle(int x, int y) {
		map.addThing("bush", x, y);
	}
}
