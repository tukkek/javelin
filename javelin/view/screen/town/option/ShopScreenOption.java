package javelin.view.screen.town.option;

import javelin.model.world.place.town.Town;
import javelin.view.screen.shopping.TownShopScreen;
import javelin.view.screen.town.SelectScreen;

/**
 * @see TownShopScreen
 * @author alex
 */
public class ShopScreenOption extends ScreenOption {

	public ShopScreenOption(String name, Town t, char c) {
		super(name, t, c);
	}

	@Override
	public SelectScreen show() {
		return new TownShopScreen(t);
	}

}
