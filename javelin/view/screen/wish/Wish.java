package javelin.view.screen.wish;

import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.view.screen.Option;

/**
 * A trick to be done by {@link Haxor}.
 *
 * @see WishScreen
 * @author alex
 */
public abstract class Wish extends Option {
	final boolean requirestarget;
	protected WishScreen screen;

	/**
	 * See {@link Option#Option(String, double, Character)}.
	 *
	 * @param requirestargetp
	 *            <code>true</code> if should ask for a {@link Squad}
	 *            {@link Combatant} as target of this effect.
	 */
	public Wish(String name, Character keyp, double price,
			boolean requirestargetp, WishScreen screen) {
		super(name, price, keyp);
		requirestarget = requirestargetp;
		this.screen = screen;
	}

	/**
	 * @param target
	 *            Provided if this hack {@link #requirestarget}.
	 * @return <code>true</code> if successful and rubies can be deduced.
	 */
	protected abstract boolean wish(Combatant target);

	/**
	 * @return A String indicating why this can't be chosen, <code>null</code>
	 *         to proceed normally.
	 */
	public String validate() {
		return null;
	}
}
