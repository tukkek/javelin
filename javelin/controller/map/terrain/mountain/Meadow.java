package javelin.controller.map.terrain.mountain;

import java.awt.Image;

import javelin.controller.map.DndMap;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Meadow extends DndMap {
	/** Constructor. */
	public Meadow() {
		super("Meadow", .2, .3, 0);
		wallfloor = floor;
		wall = Images.get("terraintree");
		rock = Images.get("terrainrock3");
	}

	@Override
	public Image getobstacle() {
		return RPG.r(1, 3) <= 2 ? obstacle : rock;
	}
}
