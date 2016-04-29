package javelin.model.world.place.town;

import javelin.model.item.Item;

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
}
