package javelin.controller.terrain.map.forest;

import javelin.controller.terrain.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Medium extends DndMap {
	public Medium() {
		super(.3, .2, 0);
		wallfloor = floor;
		wall = Images.getImage("terraintreeforest");
	}

}
