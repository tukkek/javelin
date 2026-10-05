package javelin.controller.content.map.terrain.forest;

import javelin.controller.content.map.DndMap;

/**
 * @see DndMap
 */
public class SparseForest extends DndMap{
	/** Constructor. */
	public SparseForest(){
		super("Sparse forest",.3,0,0);
		MediumForest.standarize(this);
	}
}
