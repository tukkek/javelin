package javelin.controller.action.world.improvement;

import javelin.controller.action.world.Work;
import javelin.controller.terrain.Terrain;
import javelin.model.world.Improvement;
import javelin.model.world.World;
import javelin.model.world.location.Location;

/**
 * Builds a road.
 * 
 * @see Terrain#speedroad
 * @see Work
 * @author alex
 */
public class BuildRoad extends Improvement {
	/** Constructor. */
	public BuildRoad(String name, double price, Character key) {
		super(name, price, key);
	}

	@Override
	public Location done(int x, int y) {
		World.roads[x][y] = true;
		return null;
	}
}