package javelin.controller.terrain.map.forest;

import javelin.controller.terrain.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Sparse extends DndMap {
	/** Constructor. */
	public Sparse() {
		super(.3, 0, 0);
		wallfloor = floor;
		wall = Images.getImage("terraintreeforest");
	}
}
