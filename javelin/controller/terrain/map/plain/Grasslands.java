package javelin.controller.terrain.map.plain;

import javelin.controller.terrain.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Grasslands extends DndMap {
	/** Constructor. */
	public Grasslands() {
		super(.1, .2, 0);
		floor = Images.getImage("terrainplains");
		wallfloor = floor;
		wall = Images.getImage("terraintree");
		obstacle = Images.getImage("terrainbush2");
	}

}
