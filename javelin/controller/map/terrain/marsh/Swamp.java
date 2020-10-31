package javelin.controller.map.terrain.marsh;

import java.util.List;

import javelin.controller.Weather;
import javelin.controller.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Swamp extends DndMap{
	/** Constructor. */
	public Swamp(){
		super("Swamp",.2,.2,.6);
		floor=Images.get(List.of("terrain","marsh"));
		wallfloor=floor;
		maxflooding=Weather.CLEAR;
		obstacle=rock;
		wall=Images.get(List.of("terrain","tree"));
	}

}
