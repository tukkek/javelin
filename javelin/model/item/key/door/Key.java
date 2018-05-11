package javelin.model.item.key.door;

import javelin.model.item.Item;

public class Key extends Item {
	public Key(String name) {
		super(name, 0, null);
		usedinbattle = false;
		usedoutofbattle = false;
	}
}