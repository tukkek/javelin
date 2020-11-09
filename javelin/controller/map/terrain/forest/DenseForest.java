package javelin.controller.map.terrain.forest;

import javelin.controller.map.DndMap;

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
