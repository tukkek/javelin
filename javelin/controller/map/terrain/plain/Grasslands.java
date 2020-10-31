package javelin.controller.map.terrain.plain;

import java.util.List;

import javelin.controller.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Grasslands extends DndMap{
	/** Constructor. */
	public Grasslands(){
		super("Grasslands",.1,.2,0);
		floor=Images.get(List.of("terrain","forestfloor2"));
		wallfloor=floor;
		wall=Images.get(List.of("terrain","tree"));
		obstacle=Images.get(List.of("terrain","bush2"));
	}
}
