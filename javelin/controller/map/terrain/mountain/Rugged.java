package javelin.controller.map.terrain.mountain;

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
		super("Rugged mountain", .3, .2, 0);
		rock = Images.get("terrainrock2");
		wall = Images.get("terrainruggedwall");
		standard = false;
	}

	@Override
	public Image getobstacle() {
		return RPG.r(1, 3) <= 2 ? obstacle : rock;
	}
}
