package javelin.controller.map.desert;

import javelin.controller.Weather;
import javelin.controller.map.DndMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Tile;

/**
 * @see DndMap
 */
public class Rocky extends DndMap {
	public Rocky() {
		super(0, .6, 0);
		floor = Tile.MUDFLOOR;
		maxflooding = Weather.DRY;
	}

	@Override
	public void putobstacle(int x, int y) {
		map.addThing(Lib.create(RPG.r(1, 6) <= 1 ? "bush" : "stone bench"), x,
				y);
	}
}
