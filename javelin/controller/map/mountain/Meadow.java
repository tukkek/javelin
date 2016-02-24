package javelin.controller.map.mountain;

import javelin.controller.map.DndMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;

/**
 * @see DndMap
 */
public class Meadow extends DndMap {
	public Meadow() {
		super(.2, .3, 0);
	}

	@Override
	public void putwall(int x, int y) {
		map.addThing(Lib.create("tree"), x, y);
	}

	@Override
	public void putobstacle(int x, int y) {
		map.addThing(Lib.create(RPG.r(1, 3) <= 2 ? "bush" : "stone bench"), x,
				y);
	}
}
