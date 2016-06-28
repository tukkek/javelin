package javelin.controller.terrain;

import javelin.controller.terrain.map.Maps;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * See {@link Terrain#UNDERGROUND}.
 * 
 * @author alex
 */
public class Underground extends Terrain {
	/** Constructor. */
	public Underground() {
		name = "underground";
	}

	@Override
	public Maps getmaps() {
		return Dungeon.getmaps();
	}
}
