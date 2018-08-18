package javelin.controller.map.terrain.water;

import javelin.controller.map.DndMap;
import javelin.view.Images;

/**
 * 100% water map.
 * 
 * @author alex
 */
public class DeepWaters extends DndMap {
	/** Constructor. */
	public DeepWaters() {
		super("Water", 0, 0, 1);
		flooded = Images.getImage("terrainaquatic");
		standard = false;
	}
}
