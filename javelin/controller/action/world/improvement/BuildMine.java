package javelin.controller.action.world.improvement;

import javelin.controller.action.world.Work;
import javelin.model.world.Improvement;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Mine;

/**
 * Builds a {@link Mine}.
 * 
 * @see Work
 * @author alex
 */
public class BuildMine extends Improvement {
	/**
	 * Constructor.
	 */
	public BuildMine(String name, double price, Character key, boolean b) {
		super(name, price, key, b);
	}

	@Override
	public Location done(int x, int y) {
		Mine m = new Mine(x, y);
		m.setlocation(x, y);
		return m;
	}
}