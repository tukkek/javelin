package javelin.model.world.location.order;

import javelin.model.item.Item;
import javelin.model.item.Potion;

/**
 * {@link Item} in the process of being completed.
 * 
 * @author alex
 */
public class CraftingOrder extends Order {
	/** Item to be done at {@link Order#completionat}. */
	public Item item;

	/**
	 * Constructor.
	 * 
	 * @param namep
	 */
	public CraftingOrder(long completionat, Item item, String namep) {
		super(completionat, namep);
		this.item = item;
	}

	public CraftingOrder(Item i) {
		super(estimate(i), i.name);
		item = i.clone();
	}

	static long estimate(Item i) {
		if (i instanceof Potion) {
			return 24;
		}
		return Math.max(24, 24 * i.price / 1000);
	}
}
