package javelin.controller.map.forest;

import javelin.controller.map.DndMap;
import tyrant.mikera.engine.Lib;

/**
 * @see DndMap
 */
public class DenseForest extends DndMap {
	public DenseForest() {
		super(.4, .3, 0);
	}

	@Override
	public void putwall(int x, int y) {
		map.addThing(Lib.create("tree"), x, y);
	}
}
