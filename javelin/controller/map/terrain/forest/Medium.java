package javelin.controller.map.terrain.forest;

import java.util.List;

import javelin.controller.map.DndMap;
import javelin.controller.map.Map;
import javelin.controller.terrain.Forest;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Medium extends DndMap{
	/** Constructor. */
	public Medium(){
		super("Forest",.3,.2,0);
		standarize(this);
	}

	/** Sets {@link Forest} look and feel. */
	public static void standarize(Map m){
		m.wallfloor=m.floor;
		m.wall=Images.get(List.of("terrain","treeforest"));
	}
}
