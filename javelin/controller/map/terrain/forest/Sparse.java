package javelin.controller.map.terrain.forest;

import javelin.controller.map.DndMap;

/**
 * @see DndMap
 */
public class Sparse extends DndMap{
	/** Constructor. */
	public Sparse(){
		super("Sparse forest",.3,0,0);
		Medium.standarize(this);
	}
}
