package javelin.view.screen.haxor;

import javelin.model.unit.Combatant;
import javelin.model.world.place.unique.Haxor;
import javelin.view.screen.Option;

/**
 * A trick to be done by {@link Haxor}.
 * 
 * @see HaxorScreen
 * @author alex
 */
public abstract class Hax extends Option {
	final boolean requirestarget;

	public Hax(String name, double price, boolean requirestargetp) {
		super(name, price);
		requirestarget = requirestargetp;
	}

	/**
	 * @param target
	 *            Provided if this hack {@link #requirestarget}.
	 * @return <code>true</code> if successful and tickets can be deduced.
	 * @see Haxor#tickets
	 */
	protected abstract boolean hack(Combatant target, HaxorScreen s);
}
