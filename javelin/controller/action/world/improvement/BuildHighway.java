package javelin.controller.action.world.improvement;

import javelin.controller.action.world.Work;
import javelin.controller.terrain.Terrain;
import javelin.model.world.Improvement;
import javelin.model.world.World;
import javelin.model.world.location.Location;

/**
 * Upgrades the work of {@link BuildRoad}.
 * 
 * @see Work
 * @see Terrain#speedhighway
 * @author alex
 */
public class BuildHighway extends Improvement {
	/**
	 * Constructor.
	 */
	public BuildHighway(String name, double price, Character key, boolean b) {
		super(name, price, key, b, false);
	}

	@Override
	public Location done(int x, int y) {
		World.highways[x][y] = true;
		return null;
	}
}