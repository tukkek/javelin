package javelin.controller.terrain.map.hill;

import java.awt.Image;

import javelin.controller.terrain.map.DndMap;
import javelin.view.Images;
import tyrant.mikera.engine.RPG;

/**
 * @see DndMap
 */
public class Rugged extends DndMap {
	/** Constructor. */
	public Rugged() {
		super("Rugged hill", .1, .7, 0);
		floor = Images.getImage("terrainforestfloor");
		wallfloor = floor;
		wall = Images.getImage("terraintree");
	}

	@Override
	public Image getobstacle() {
		return RPG.r(1, 7) <= 2 ? obstacle : rock;
	}
}
