package javelin.controller.content.map.terrain.marsh;

import java.util.List;

import javelin.controller.Weather;
import javelin.controller.content.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Moor extends DndMap{
	/** Constructor. */
	public Moor(){
		super("Moor",.1,.3,.3);
		floor=Images.get(List.of("terrain","marsh"));
		wallfloor=floor;
		maxflooding=Weather.CLEAR;
		wall=Images.get(List.of("terrain","tree"));
	}

}
