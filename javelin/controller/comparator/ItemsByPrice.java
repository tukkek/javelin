package javelin.controller.comparator;

import java.util.Comparator;
import java.util.List;

import javelin.model.item.Item;

public class ItemsByPrice implements Comparator<Item> {
	/**
	 * Organizes a {@link List} from cheapest to most expensive Item.
	 *
	 * @see List#sort(Comparator)
	 */
	public static Comparator<Item> SINGLETON = new ItemsByPrice();

	private ItemsByPrice() {
		// use SINGLETON
	}

	@Override
	public int compare(Item o1, Item o2) {
		return o1.price - o2.price;
	}
}