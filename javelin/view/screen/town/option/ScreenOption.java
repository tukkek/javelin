package javelin.view.screen.town.option;

import javelin.model.world.town.Town;
import javelin.view.screen.town.SelectScreen;

/**
 * @see SelectScreen
 * @author alex
 */
abstract public class ScreenOption extends Option {
	protected final Town t;

	public ScreenOption(String name, Town t) {
		super(name, 0);
		this.t = t;
	}

	abstract public SelectScreen show();
}
