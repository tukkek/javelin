package javelin.controller.map.terrain.plain;

import javelin.controller.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Farm extends DndMap {
	/** Constructor. */
	public Farm() {
		super("Farm", .1, .4, 0);
		floor = Images.get("terrainforestfloor");
		wallfloor = floor;
		wall = Images.get("terraintree");
		obstacle = Images.get("terrainbush2");
	}

}
