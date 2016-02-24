package javelin.controller.map.plain;

import javelin.controller.map.DndMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.tyrant.Tile;

/**
 * @see DndMap
 */
public class Battlefield extends DndMap {
	public Battlefield() {
		super(.1, .2, 0);
		floor = Tile.FORESTFLOOR;
	}

	@Override
	public void putobstacle(int x, int y) {
		map.addThing(Lib.create("stone bench"), x, y);
		// map.setTile(x, y, Tile.STONEWALL);
	}

	@Override
	public void putwall(int x, int y) {
		map.addThing(Lib.create("tree"), x, y);
	}
}
