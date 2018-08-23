package javelin.controller.map.terrain.hill;

import java.awt.Image;

import javelin.controller.map.DndMap;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Gentle extends DndMap {
	/** Constructor. */
	public Gentle() {
		super("Gentle hill", .1, .4, 0);
		floor = Images.get("terrainforestfloor");
		wallfloor = floor;
		wall = Images.get("terraintree");
	}

	@Override
	public Image getobstacle() {
		return RPG.r(1, 2) == 1 ? rock : obstacle;
	}
}
