package javelin.controller.map.terrain.plain;

import java.util.List;

import javelin.controller.map.DndMap;
import javelin.view.Images;

/**
 * Open field.
 *
 * @see DndMap
 */
public class Field extends DndMap{
	/** Constructor. */
	public Field(){
		super("Battlefield",.1,.2,0);
		floor=Images.get(List.of("terrain","forestfloor"));
		wallfloor=floor;
		obstacle=rock;
		wall=Images.get(List.of("terrain","tree"));
		obstacle=Images.get(List.of("terrain","bush2"));
	}

}
