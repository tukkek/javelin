package javelin.controller.map.hill;

import javelin.controller.map.DndMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Tile;

/**
 * @see DndMap
 */
public class RuggedHill extends DndMap {
	public RuggedHill() {
		super(.1, .7, 0);
		floor = Tile.FORESTFLOOR;
	}

	@Override
	public void putwall(int x, int y) {
		map.addThing(Lib.create("tree"), x, y);
	}

	@Override
	public void putobstacle(int x, int y) {
		map.addThing(Lib.create(RPG.r(1, 7) <= 2 ? "bush" : "stone bench"), x,
				y);
	}
}
