package javelin.controller.map;

import java.util.List;

import javelin.view.Images;

/**
 * Empty battle grounds, represents a big empty sports arena.
 *
 * @author alex
 */
public class Stadium extends DndMap{
	/** Constructor. */
	public Stadium(){
		super("Arena",0,0,0);
		floor=Images.get(List.of("terrain","arena"));
	}
}