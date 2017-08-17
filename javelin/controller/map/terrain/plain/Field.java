package javelin.controller.map.terrain.plain;

import javelin.controller.map.DndMap;
import javelin.model.world.location.unique.minigame.Battlefield;
import javelin.view.Images;

/**
 * The proper term in the d20 SRF is "Battlefield" but renamed here to avoid
 * collision with {@link Battlefield}.
 * 
 * @see DndMap
 */
public class Field extends DndMap {
	/** Constructor. */
	public Field() {
		super("Battlefield", .1, .2, 0);
		floor = Images.getImage("terrainforestfloor");
		wallfloor = floor;
		obstacle = rock;
		wall = Images.getImage("terraintree");
		obstacle = Images.getImage("terrainbush2");
	}

}
