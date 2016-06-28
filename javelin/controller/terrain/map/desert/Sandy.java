package javelin.controller.terrain.map.desert;

import javelin.controller.Weather;
import javelin.controller.terrain.map.DndMap;
import javelin.model.world.Season;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Sandy extends DndMap {
	public Sandy() {
		super(0, .1, 0);
		floor = Images.getImage("terraindesert");
		maxflooding = Weather.DRY;
		obstacle = rock;
	}

	@Override
	public boolean validate() {
		return Season.current != Season.WINTER;
	}
}
