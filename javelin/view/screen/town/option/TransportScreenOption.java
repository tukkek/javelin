package javelin.view.screen.town.option;

import javelin.model.world.Town;
import javelin.view.screen.town.SelectScreen;
import javelin.view.screen.town.TransportScreen;

/**
 * @see TransportScreen
 * @author alex
 */
public class TransportScreenOption extends ScreenOption {
	public TransportScreenOption(String name, Town t) {
		super(name, t);
	}

	@Override
	public SelectScreen show() {
		return new TransportScreen("Rent:", t);
	}
}
