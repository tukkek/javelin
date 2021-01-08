package javelin.controller.content.map.terrain.water;

import java.util.List;

import javelin.controller.content.map.DndMap;
import javelin.view.Images;

/**
 * 100% water map.
 *
 * @author alex
 */
public class DeepWaters extends DndMap{
	/** Constructor. */
	public DeepWaters(){
		super("Water",0,0,1);
		flooded=Images.get(List.of("terrain","aquatic"));
		standard=false;
	}
}
