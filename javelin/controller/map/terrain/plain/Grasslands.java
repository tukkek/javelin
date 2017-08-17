package javelin.controller.map.terrain.plain;

import javelin.controller.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Grasslands extends DndMap {
	/** Constructor. */
	public Grasslands() {
		super("Grasslands", .1, .2, 0);
		floor = Images.getImage("terrainplains");
		wallfloor = floor;
		wall = Images.getImage("terraintree");
		obstacle = Images.getImage("terrainbush2");
	}

}
