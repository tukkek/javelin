package javelin.controller.map.terrain.marsh;

import javelin.controller.Weather;
import javelin.controller.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Moor extends DndMap {
	/** Constructor. */
	public Moor() {
		super("Moor", .1, .3, .3);
		floor = Images.getImage("terrainmarsh");
		wallfloor = floor;
		maxflooding = Weather.DRY;
		wall = Images.getImage("terraintree");
	}

}
