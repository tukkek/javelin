package javelin.controller.content.map.terrain.plain;

import java.util.List;

import javelin.controller.content.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Farm extends DndMap{
	/** Constructor. */
	public Farm(){
		super("Farm",.1,.4,0);
		floor=Images.get(List.of("terrain","forestfloor"));
		wallfloor=floor;
		wall=Images.get(List.of("terrain","tree"));
		obstacle=Images.get(List.of("terrain","bush2"));
	}

}
