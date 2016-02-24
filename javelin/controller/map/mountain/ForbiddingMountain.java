package javelin.controller.map.mountain;

import javelin.controller.map.DndMap;
import tyrant.mikera.tyrant.Tile;

/**
 * @see DndMap
 */
public class ForbiddingMountain extends DndMap {
	public ForbiddingMountain() {
		super(.7, 0, 0);
		floor = Tile.PLAINS;
	}

	@Override
	public void putwall(int x, int y) {
		map.setTile(x, y, Tile.CAVEWALL);
	}

}
