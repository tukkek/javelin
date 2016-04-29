package javelin.view.screen.shopping;

import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.potion.Potion;
import javelin.model.world.Squad;
import javelin.model.world.place.town.CraftingOrder;
import javelin.model.world.place.town.Town;
import javelin.view.screen.town.PurchaseOption;

/**
 * {@link ShoppingScreen} to be used inside {@link Town}s.
 * 
 * @author alex
 */
public class TownShopScreen extends ShoppingScreen {
	/** Constructor. */
	public TownShopScreen(final Town town) {
		super("Buy:", town);
	}

	@Override
	protected ItemSelection getitems() {
		return town.items;
	}

	@Override
	protected void afterpurchase(final PurchaseOption o) {
		Item i = o.i;
		long last = town.crafting.queue.isEmpty() ? 0
				: town.crafting.last().completionat - Squad.active.hourselapsed;
		town.crafting.add(new CraftingOrder((i instanceof Potion ? 24
				: Math.max(24, 24 * Math.round(i.price / 1000f))) + last,
				(Item) i.clone(), i.toString()));
	}
}
