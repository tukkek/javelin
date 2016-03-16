package javelin.view.screen.town.option;

import javelin.model.world.town.Town;
import javelin.view.screen.town.SelectScreen;
import javelin.view.screen.town.ShoppingScreen;

/**
 * @see ShoppingScreen
 * @author alex
 */
public class ShopScreenOption extends ScreenOption {

	public ShopScreenOption(String name, Town t) {
		super(name, t);
	}

	@Override
	public SelectScreen show() {
		return new ShoppingScreen(t);
	}

}
