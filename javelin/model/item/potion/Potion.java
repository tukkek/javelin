package javelin.model.item.potion;

import javelin.model.item.Item;
import javelin.model.item.ItemSelection;

/**
 * Represent a consumable potion to be used in-battle. Any monster can use a
 * potion.
 * 
 * @author alex
 */
public abstract class Potion extends Item {
	public Potion(final String name, final int price, ItemSelection town) {
		super(name, price, town);
	}

}
