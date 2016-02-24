package javelin.controller.map.plain;

import javelin.controller.map.DndMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.tyrant.Tile;

/**
 * @see DndMap
 */
public class Farm extends DndMap {
	public Farm() {
		super(.1, .4, 0);
		floor = Tile.FORESTFLOOR;
	}

	@Override
	public void putwall(int x, int y) {
		map.addThing(Lib.create("tree"), x, y);
	}
}
