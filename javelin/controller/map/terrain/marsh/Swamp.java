package javelin.controller.map.terrain.marsh;

import javelin.controller.Weather;
import javelin.controller.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Swamp extends DndMap {
	/** Constructor. */
	public Swamp() {
		super("Swamp", .2, .2, .6);
		floor = Images.get("terrainmarsh");
		wallfloor = floor;
		maxflooding = Weather.DRY;
		obstacle = rock;
		wall = Images.get("terraintree");
	}

}
