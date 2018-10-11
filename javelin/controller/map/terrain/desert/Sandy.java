package javelin.controller.map.terrain.desert;

import javelin.controller.Weather;
import javelin.controller.map.DndMap;
import javelin.model.world.Season;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Sandy extends DndMap{
	/** Constructor. */
	public Sandy(){
		super("Sandy desert",0,.1,0);
		floor=Images.get("terraindesert");
		maxflooding=Weather.DRY;
		obstacle=rock;
	}

	@Override
	public boolean validate(){
		return Season.current!=Season.WINTER;
	}
}
