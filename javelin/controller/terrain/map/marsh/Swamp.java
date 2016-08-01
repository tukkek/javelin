package javelin.controller.terrain.map.marsh;

import javelin.controller.Weather;
import javelin.controller.terrain.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Swamp extends DndMap {
	/** Constructor. */
	public Swamp() {
		super("Swamp", .2, .2, .6);
		floor = Images.getImage("terrainmarsh");
		wallfloor = floor;
		maxflooding = Weather.DRY;
		obstacle = rock;
		wall = Images.getImage("terraintree");
	}

}
