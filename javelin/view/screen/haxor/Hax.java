package javelin.view.screen.haxor;

import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.unique.Haxor;
import javelin.view.screen.Option;

/**
 * A trick to be done by {@link Haxor}.
 * 
 * @see HaxorScreen
 * @author alex
 */
public abstract class Hax extends Option {
	final boolean requirestarget;

	/**
	 * See {@link Option#Option(String, double, Character)}.
	 * 
	 * @param requirestargetp
	 *            <code>true</code> if should ask for a {@link Squad}
	 *            {@link Combatant} as target of this effect.
	 */
	public Hax(String name, Character keyp, double price,
			boolean requirestargetp) {
		super(name, price, keyp);
		requirestarget = requirestargetp;
	}

	/**
	 * @param target
	 *            Provided if this hack {@link #requirestarget}.
	 * @return <code>true</code> if successful and tickets can be deduced.
	 * @see Haxor#rubies
	 */
	protected abstract boolean hack(Combatant target, HaxorScreen s);
}
