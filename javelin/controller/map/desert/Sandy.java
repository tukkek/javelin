package javelin.controller.map.desert;

import javelin.controller.Weather;
import javelin.controller.map.DndMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.tyrant.Tile;

/**
 * @see DndMap
 */
public class Sandy extends DndMap {
	public Sandy() {
		super(0, .1, 0);
		floor = Tile.MUDFLOOR;
		maxflooding = Weather.DRY;
	}

	@Override
	public void putobstacle(int x, int y) {
		map.addThing(Lib.create("stone bench"), x, y);
	}
}
