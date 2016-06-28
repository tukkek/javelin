package javelin.view.screen.town.option;

import javelin.model.world.location.town.Town;
import javelin.view.screen.town.SelectScreen;
import javelin.view.screen.town.TransportScreen;

/**
 * @see TransportScreen
 * @author alex
 */
public class TransportScreenOption extends ScreenOption {
	public TransportScreenOption(String name, Town t, char c) {
		super(name, t, c);
	}

	@Override
	public SelectScreen show() {
		return new TransportScreen("Rent:", t);
	}
}
