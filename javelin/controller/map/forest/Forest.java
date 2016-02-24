package javelin.controller.map.forest;

import javelin.controller.map.DndMap;
import tyrant.mikera.engine.Lib;

/**
 * @see DndMap
 */
public class Forest extends DndMap {
	public Forest() {
		super(.3, .2, 0);
	}

	@Override
	public void putwall(int x, int y) {
		map.addThing(Lib.create("tree"), x, y);
	}
}
