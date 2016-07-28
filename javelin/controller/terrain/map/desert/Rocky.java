package javelin.controller.terrain.map.desert;

import java.awt.Image;

import javelin.controller.Weather;
import javelin.controller.terrain.map.DndMap;
import javelin.model.world.Season;
import javelin.view.Images;
import tyrant.mikera.engine.RPG;

/**
 * @see DndMap
 */
public class Rocky extends DndMap {
	/** Constructor. */
	public Rocky() {
		super(0, .6, 0);
		floor = Images.getImage("terraindesert");
		maxflooding = Weather.DRY;
	}

	@Override
	public Image getobstacle() {
		return RPG.r(1, 6) <= 1 ? obstacle : rock;
	}

	@Override
	public boolean validate() {
		return Season.current != Season.WINTER;
	}
}
