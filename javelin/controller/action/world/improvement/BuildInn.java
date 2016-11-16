package javelin.controller.action.world.improvement;

import javelin.controller.action.world.Work;
import javelin.model.world.Improvement;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Inn;

/**
 * Builds an {@link Inn}.
 * 
 * @see Work
 * @author alex
 */
public class BuildInn extends Improvement {
	/**
	 * Constructor.
	 */
	public BuildInn(String name, double price, Character key, boolean b) {
		super(name, price, key, b, true);
	}

	@Override
	public Location done(int x, int y) {
		Location i = new Inn();
		i.setlocation(x, y);
		return i;
	}
}