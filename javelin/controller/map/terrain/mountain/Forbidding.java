package javelin.controller.map.terrain.mountain;

import javelin.controller.map.DndMap;
import javelin.view.Images;

/**
 * TODO should be .7 walls but then we need to make sure there are paths between
 * different areas of the map
 *
 * @see DndMap
 */
public class Forbidding extends DndMap{
	/** Constructor. */
	public Forbidding(){
		super("Forbidding mountain",.4,0,0);
		floor=Images.get("terrainruggedwall");
		wall=Images.get("terrainorcwall");
	}
}
