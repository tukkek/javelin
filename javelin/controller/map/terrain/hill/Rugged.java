package javelin.controller.map.terrain.hill;

import java.awt.Image;

import javelin.controller.map.DndMap;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Rugged extends DndMap {
	/** Constructor. */
	public Rugged() {
		super("Rugged hill", .1, .7, 0);
		floor = Images.get("terrainforestfloor");
		wallfloor = floor;
		wall = Images.get("terraintree");
	}

	@Override
	public Image getobstacle() {
		return RPG.r(1, 7) <= 2 ? obstacle : rock;
	}
}
