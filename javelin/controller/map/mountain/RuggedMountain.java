package javelin.controller.map.mountain;

import javelin.controller.map.DndMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Tile;

/**
 * @see DndMap
 */
public class RuggedMountain extends DndMap {
	public RuggedMountain() {
		super(.3, .2, 0);
	}

	@Override
	public void putwall(int x, int y) {
		map.setTile(x, y, Tile.CAVEWALL);
	}

	@Override
	public void putobstacle(int x, int y) {
		map.addThing(Lib.create(RPG.r(1, 3) <= 2 ? "bush" : "stone bench"), x,
				y);
	}
}
