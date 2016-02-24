package javelin.controller.map.forest;

import javelin.controller.map.DndMap;
import tyrant.mikera.engine.Lib;

/**
 * @see DndMap
 */
public class SparseForest extends DndMap {
	public SparseForest() {
		super(.3, 0, 0);
	}

	@Override
	public void putwall(int x, int y) {
		map.addThing(Lib.create("tree"), x, y);
	}
}
