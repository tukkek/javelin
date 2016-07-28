package javelin.controller.terrain.map.forest;

import javelin.controller.terrain.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Dense extends DndMap {
	/** Constructor. */
	public Dense() {
		super(.4, .3, 0);
		wallfloor = floor;
		wall = Images.getImage("terraintreeforest");
	}
}
