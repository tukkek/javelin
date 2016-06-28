package javelin.controller.terrain.map;

import javelin.view.Images;

/**
 * 100% water map.
 * 
 * @author alex
 */
public class Water extends DndMap {
	/** Constructor. */
	public Water() {
		super(0, 0, 1);
		flooded = Images.getImage("terrainaquatic");
	}
}
