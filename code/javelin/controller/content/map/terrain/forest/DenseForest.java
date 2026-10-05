package javelin.controller.content.map.terrain.forest;

import javelin.controller.content.map.DndMap;

/**
 * @see DndMap
 */
public class DenseForest extends DndMap{
	/** Constructor. */
	public DenseForest(){
		super("Dense forest",.4,.3,0);
		MediumForest.standarize(this);
	}
}
