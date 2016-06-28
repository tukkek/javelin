package javelin.controller.terrain.map.mountain;

import java.awt.Image;

import javelin.controller.terrain.map.DndMap;
import javelin.view.Images;
import tyrant.mikera.engine.RPG;

/**
 * @see DndMap
 */
public class Rugged extends DndMap {
	public Rugged() {
		super(.3, .2, 0);
		rock = Images.getImage("terrainrock2");
	}

	@Override
	public Image getobstacle() {
		return RPG.r(1, 3) <= 2 ? obstacle : rock;
	}
}
