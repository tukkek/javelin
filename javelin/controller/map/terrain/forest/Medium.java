package javelin.controller.map.terrain.forest;

import javelin.controller.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Medium extends DndMap {
	/** Constructor. */
	public Medium() {
		super("Forest", .3, .2, 0);
		wallfloor = floor;
		wall = Images.get("terraintreeforest");
	}

}
