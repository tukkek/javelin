package javelin.view.screen.shopping;

import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.world.Merchant;
import javelin.model.world.Squad;
import javelin.model.world.place.town.Town;
import javelin.view.screen.town.PurchaseOption;

/**
 * Used when a {@link Squad} meets a {@link Merchant}.
 * 
 * @author alex
 */
public class MerchantScreen extends ShoppingScreen {

	final Merchant m;

	public MerchantScreen(Merchant m) {
		super("You reach a trading caravan:", null);
		this.m = m;
	}

	@Override
	protected void afterpurchase(PurchaseOption o) {
		m.inventory.remove(o.i);
		Town.grab((Item) o.i.clone());
	}

	@Override
	protected ItemSelection getitems() {
		return m.inventory;
	}

}
