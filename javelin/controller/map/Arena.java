package javelin.controller.map;

import tyrant.mikera.tyrant.Tile;

/**
 * Empty battle grounds, represents a big empty sports arena.
 * 
 * @author alex
 */
public class Arena extends DndMap {
	public Arena(int width, int heigth) {
		super(0, 0, 0);
		floor = Tile.FORESTFLOOR;
	}
}