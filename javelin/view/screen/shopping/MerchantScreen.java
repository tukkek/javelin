package javelin.view.screen.shopping;

import javelin.model.item.ItemSelection;
import javelin.model.unit.Squad;
import javelin.model.world.Caravan;
import javelin.view.screen.town.PurchaseOption;

/**
 * Used when a {@link Squad} meets a {@link Caravan}.
 * 
 * @author alex
 */
public class MerchantScreen extends ShoppingScreen {

	final Caravan m;

	public MerchantScreen(Caravan m) {
		super("You reach a trading caravan:", null);
		this.m = m;
	}

	@Override
	protected void afterpurchase(PurchaseOption o) {
		m.inventory.remove(o.i);
		o.i.clone().grab();
	}

	@Override
	protected ItemSelection getitems() {
		return m.inventory;
	}

}
