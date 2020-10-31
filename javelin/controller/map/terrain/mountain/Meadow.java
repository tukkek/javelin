package javelin.controller.map.terrain.mountain;

import java.awt.Image;
import java.util.List;

import javelin.controller.map.DndMap;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Meadow extends DndMap{
	/** Constructor. */
	public Meadow(){
		super("Meadow",.2,.3,0);
		wallfloor=floor;
		wall=Images.get(List.of("terrain","tree"));
		rock=Images.get(List.of("terrain","rock3"));
	}

	@Override
	public Image getobstacle(int x,int y){
		return RPG.r(1,3)<=2?obstacle:rock;
	}
}
