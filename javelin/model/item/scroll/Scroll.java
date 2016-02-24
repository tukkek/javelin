package javelin.model.item.scroll;

import javelin.model.item.Item;
import javelin.model.item.ItemSelection;

/**
 * Can only be used out-of-combat.
 * 
 * @author alex
 */
public abstract class Scroll extends Item {
	/**
	 * @see Item#Item(String, int, ItemSelection)
	 */
	public Scroll(final String name, final int price,
			final ItemSelection town) {
		super(name, price, town);
	}

	@Override
	public boolean isusedinbattle() {
		return false;
	}
}
