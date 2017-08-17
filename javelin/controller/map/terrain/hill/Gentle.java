package javelin.controller.map.terrain.hill;

import java.awt.Image;

import javelin.controller.map.DndMap;
import javelin.view.Images;
import tyrant.mikera.engine.RPG;

/**
 * @see DndMap
 */
public class Gentle extends DndMap {
	/** Constructor. */
	public Gentle() {
		super("Gentle hill", .1, .4, 0);
		floor = Images.getImage("terrainforestfloor");
		wallfloor = floor;
		wall = Images.getImage("terraintree");
	}

	@Override
	public Image getobstacle() {
		return RPG.r(1, 2) == 1 ? rock : obstacle;
	}
}
