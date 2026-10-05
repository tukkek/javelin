package javelin.controller.content.map.terrain.mountain;

import java.util.List;

import javelin.controller.content.map.DndMap;
import javelin.view.Images;

/**
 * TODO should be .7 walls but then we need to make sure there are paths between
 * different areas of the map
 *
 * @see DndMap
 */
public class ForbiddingMountain extends DndMap{
	/** Constructor. */
	public ForbiddingMountain(){
		super("Forbidding mountain",.4,0,0);
		floor=Images.get(List.of("terrain","ruggedwall"));
		wall=Images.get(List.of("terrain","orcwall"));
	}
}
