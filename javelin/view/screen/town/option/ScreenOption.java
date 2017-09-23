package javelin.view.screen.town.option;

import javelin.model.world.location.town.Town;
import javelin.view.screen.Option;
import javelin.view.screen.town.SelectScreen;

/**
 * @see SelectScreen
 * @author alex
 */
abstract public class ScreenOption extends Option {
	/** Helper object since screen options often are used in towns. */
	protected final Town t;

	/** Constructor with null {@link #t}. */
	public ScreenOption(String name, Town t) {
		this(name, t, null);
	}

	/** Constructor. */
	public ScreenOption(String string, Town town, Character c) {
		super(string, 0, c);
		this.t = town;
	}

	/**
	 * @return Shows the screen.
	 */
	abstract public SelectScreen getscreen();
}
