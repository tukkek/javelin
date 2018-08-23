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
public class Tundra extends DndMap {
	/** Constructor. */
	public Tundra() {
		super("Tundra", 0, .4, 0);
		floor = Images.get("terrainice");
		maxflooding = Weather.DRY;
	}

	@Override
	public Image getobstacle() {
		return RPG.r(1, 4) == 1 ? obstacle : rock;
	}

	@Override
	public boolean validate() {
		return Season.current == Season.WINTER;
	}
}
