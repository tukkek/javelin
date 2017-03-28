package javelin.model.world.location.order;

import javelin.model.item.Item;
import javelin.model.item.Potion;
import javelin.model.unit.Squad;

/**
 * {@link Item} in the process of being completed.
 *
 * @author alex
 */
public class CraftingOrder extends Order {
	/** Item to be done at {@link Order#completionat}. */
	public Item item;

	// /**
	// * Constructor.
	// *
	// * @param namep
	// */
	// public CraftingOrder(long completionat, Item item, String namep) {
	// super(completionat, namep);
	// this.item = item.clone();
	// }

	public CraftingOrder(Item i, OrderQueue queue) {
		super(i instanceof Potion ? 24 : Math.max(24, 24 * i.price / 1000),
				i.name);
		item = i.clone();
		if (queue != null && !queue.queue.isEmpty()) {
			completionat += queue.last().completionat
					- Squad.active.hourselapsed;
		}
	}
}
