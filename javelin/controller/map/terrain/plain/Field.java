package javelin.controller.map.terrain.plain;

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
		floor=Images.get("terrainforestfloor");
		wallfloor=floor;
		obstacle=rock;
		wall=Images.get("terraintree");
		obstacle=Images.get("terrainbush2");
	}

}
