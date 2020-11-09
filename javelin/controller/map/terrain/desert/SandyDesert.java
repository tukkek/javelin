package javelin.controller.map.terrain.desert;

import java.util.List;

import javelin.controller.Weather;
import javelin.controller.map.DndMap;
import javelin.model.world.Season;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class SandyDesert extends DndMap{
	/** Constructor. */
	public SandyDesert(){
		super("Sandy desert",0,.1,0);
		floor=Images.get(List.of("terrain","desert"));
		maxflooding=Weather.CLEAR;
		obstacle=rock;
	}

	@Override
	public boolean validate(){
		return Season.current!=Season.WINTER;
	}
}
