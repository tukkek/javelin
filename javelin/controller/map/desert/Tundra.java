package javelin.controller.map.desert;

import javelin.controller.Weather;
import javelin.controller.map.DndMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Tile;

/**
 * @see DndMap
 */
public class Tundra extends DndMap {
	public Tundra() {
		super(0, .4, 0);
		floor = Tile.ICEFLOOR;
		maxflooding = Weather.RAIN;
	}

	@Override
	public void putobstacle(int x, int y) {
		map.addThing(Lib.create(RPG.r(1, 4) == 1 ? "bush" : "stone bench"), x,
				y);
	}
}
