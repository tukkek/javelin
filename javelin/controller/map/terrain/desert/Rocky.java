package javelin.controller.map.terrain.desert;

import java.awt.Image;

import javelin.controller.Weather;
import javelin.controller.map.DndMap;
import javelin.model.world.Season;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Rocky extends DndMap {
	/** Constructor. */
	public Rocky() {
		super("Rocky desert", 0, .6, 0);
		floor = Images.get("terraindesert");
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
