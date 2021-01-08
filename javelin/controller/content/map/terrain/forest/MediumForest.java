package javelin.controller.content.map.terrain.forest;

import java.util.List;

import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.controller.content.terrain.Forest;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class MediumForest extends DndMap{
	/** Constructor. */
	public MediumForest(){
		super("Forest",.3,.2,0);
		standarize(this);
	}

	/** Sets {@link Forest} look and feel. */
	public static void standarize(Map m){
		m.wallfloor=m.floor;
		m.wall=Images.get(List.of("terrain","treeforest"));
	}
}
