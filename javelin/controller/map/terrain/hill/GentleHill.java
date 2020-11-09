package javelin.controller.map.terrain.hill;

import java.awt.Image;
import java.util.List;

import javelin.controller.map.DndMap;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class GentleHill extends DndMap{
	/** Constructor. */
	public GentleHill(){
		super("Gentle hill",.1,.4,0);
		floor=Images.get(List.of("terrain","forestfloor"));
		wallfloor=floor;
		wall=Images.get(List.of("terrain","tree"));
	}

	@Override
	public Image getobstacle(int x,int y){
		return RPG.r(1,2)==1?rock:obstacle;
	}
}
