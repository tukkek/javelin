package javelin.controller.map;

import javelin.view.Images;

/**
 * Empty battle grounds, represents a big empty sports arena.
 * 
 * @author alex
 */
public class Arena extends DndMap {
	/** Constructor. */
	public Arena() {
		super("Arena", 0, 0, 0);
		floor = Images.get("terrainarena");
	}
}