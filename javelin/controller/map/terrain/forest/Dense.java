package javelin.controller.map.terrain.forest;

import javelin.controller.map.DndMap;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Dense extends DndMap{
	/** Constructor. */
	public Dense(){
		super("Dense forest",.4,.3,0);
		wallfloor=floor;
		wall=Images.get("terraintreeforest");
	}
}
