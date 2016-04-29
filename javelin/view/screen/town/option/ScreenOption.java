package javelin.view.screen.town.option;

import javelin.model.world.place.town.Town;
import javelin.view.screen.Option;
import javelin.view.screen.town.SelectScreen;

/**
 * @see SelectScreen
 * @author alex
 */
abstract public class ScreenOption extends Option {
	protected final Town t;

	public ScreenOption(String name, Town t) {
		this(name, t, null);
	}

	public ScreenOption(String string, Town town, Character c) {
		super(string, 0, c);
		this.t = town;
	}

	abstract public SelectScreen show();
}
