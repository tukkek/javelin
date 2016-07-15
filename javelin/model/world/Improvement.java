package javelin.model.world;

import javelin.controller.action.world.Work;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.view.screen.Option;

/**
 * {@link Work} order.
 * 
 * @see Squad#work
 * @author alex
 */
public abstract class Improvement extends Option {
	/**
	 * If <code>true</code> will not modify the price by {@link Terrain}
	 * condition.
	 * 
	 * @see Option#price
	 * @see Terrain#getspeed()
	 */
	public boolean absolute;

	/**
	 * See {@link Option#Option(String, double, Character)}.
	 * 
	 * @param
	 */
	public Improvement(String name, double price, Character key,
			boolean absolute) {
		super(name, price, key);
		this.absolute = absolute;
	}

	/**
	 * Called when this is done, in case the {@link Squad} is still alive.
	 * 
	 * @param x
	 *            World coordinate.
	 * @param y
	 *            World coordinate.
	 * @return A location for a series of cleanups to be performed. May safely
	 *         return <code>null</code> instead.
	 */
	abstract public Location done(int x, int y);

	/**
	 * @return a string to be shown on completion or <code>null</code> for just
	 *         a completion notification.
	 */
	public String inform() {
		return null;
	}
}
