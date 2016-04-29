package javelin.view.screen.town;

import javelin.model.item.Item;
import javelin.view.screen.Option;
import javelin.view.screen.shopping.ShoppingScreen;

/**
 * Option to be used with {@link ShoppingScreen}. Carries an {@link Item}.
 * 
 * @author alex
 */
public class PurchaseOption extends Option {
	public final Item i;

	public PurchaseOption(final Item i) {
		super(i.name, i.price);
		this.i = i;
	}
}