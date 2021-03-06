package javelin.controller.terrain.map.marsh;

import javelin.controller.Weather;
import javelin.controller.terrain.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Moor extends DndMap {
	public Moor() {
		super(.1, .3, .3);
		floor = Images.getImage("terrainmarsh");
		wallfloor = floor;
		maxflooding = Weather.DRY;
		wall = Images.getImage("terraintree");
	}

}
