package javelin.controller.map.plain;

import javelin.controller.map.DndMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.tyrant.Tile;

/**
 * @see DndMap
 */
public class Grasslands extends DndMap {
	public Grasslands() {
		super(.1, .2, 0);
		floor = Tile.CAVEFLOOR;
	}

	@Override
	public void putwall(int x, int y) {
		map.addThing(Lib.create("tree"), x, y);
	}
}
