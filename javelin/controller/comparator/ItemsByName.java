package javelin.controller.comparator;

import java.util.Comparator;

import javelin.model.item.Item;

public class ItemsByName implements Comparator<Item> {
	public static final ItemsByName SINGLETON = new ItemsByName();

	private ItemsByName() {
		// use singleton
	}

	@Override
	public int compare(Item o1, Item o2) {
		return o1.name.compareTo(o2.name);
	}
}