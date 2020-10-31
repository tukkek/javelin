package javelin.controller.map.terrain.mountain;

import java.awt.Image;
import java.util.List;

import javelin.controller.map.DndMap;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * @see DndMap
 */
public class Rugged extends DndMap{
	/** Constructor. */
	public Rugged(){
		super("Rugged mountain",.3,.2,0);
		rock=Images.get(List.of("terrain","rock2"));
		wall=Images.get(List.of("terrain","ruggedwall"));
		standard=false;
	}

	@Override
	public Image getobstacle(int x,int y){
		return RPG.r(1,3)<=2?obstacle:rock;
	}
}
