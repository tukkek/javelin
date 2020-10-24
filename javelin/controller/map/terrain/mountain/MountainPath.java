package javelin.controller.map.terrain.mountain;

import javelin.controller.map.terrain.forest.ForestPath;
import javelin.controller.terrain.Mountains;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * A version of Forest Path but for {@link Mountains}.
 *
 * @author alex
 */
public class MountainPath extends ForestPath{
	/** Constructor. */
	public MountainPath(){
		wall=Images.get("terrainruggedwall");
		floor=Images.get("terraindesert");
		obstacle=Images.get("terrainrock2");
		paths=RPG.r(1,4)+4;
		river=RPG.chancein(9);
		riverwidth=new int[]{1,2};
		obstructed=RPG.r(5,20)/100f;

	}
}
