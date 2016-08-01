package javelin.controller.terrain.map.plain;

import javelin.controller.terrain.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Farm extends DndMap {
	/** Constructor. */
	public Farm() {
		super("Farm", .1, .4, 0);
		floor = Images.getImage("terrainforestfloor");
		wallfloor = floor;
		wall = Images.getImage("terraintree");
		obstacle = Images.getImage("terrainbush2");
	}

}
