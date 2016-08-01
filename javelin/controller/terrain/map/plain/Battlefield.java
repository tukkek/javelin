package javelin.controller.terrain.map.plain;

import javelin.controller.terrain.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Battlefield extends DndMap {
	/** Constructor. */
	public Battlefield() {
		super("Battlefield", .1, .2, 0);
		floor = Images.getImage("terrainforestfloor");
		wallfloor = floor;
		obstacle = rock;
		wall = Images.getImage("terraintree");
		obstacle = Images.getImage("terrainbush2");
	}

}
