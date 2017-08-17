package javelin.controller.map.terrain;

import javelin.controller.map.DndMap;
import javelin.view.Images;

/**
 * 100% water map.
 * 
 * @author alex
 */
public class Water extends DndMap {
	/** Constructor. */
	public Water() {
		super("Water", 0, 0, 1);
		flooded = Images.getImage("terrainaquatic");
		standard = false;
	}
}
