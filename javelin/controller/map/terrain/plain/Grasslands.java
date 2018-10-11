package javelin.controller.map.terrain.plain;

import javelin.controller.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Grasslands extends DndMap{
	/** Constructor. */
	public Grasslands(){
		super("Grasslands",.1,.2,0);
		floor=Images.get("terrainforestfloor2");
		wallfloor=floor;
		wall=Images.get("terraintree");
		obstacle=Images.get("terrainbush2");
	}
}
