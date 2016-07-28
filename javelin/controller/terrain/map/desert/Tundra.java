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
public class Tundra extends DndMap {
	/** Constructor. */
	public Tundra() {
		super(0, .4, 0);
		floor = Images.getImage("terrainice");
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
