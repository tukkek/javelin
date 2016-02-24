package javelin.model.dungeon;

import java.util.ArrayList;

import javelin.model.item.Item;
import javelin.model.item.Key;
import javelin.model.world.Squad;
import javelin.model.world.Town;
import javelin.view.screen.world.DungeonScreen;

/**
 * Loot!
 * 
 * @author alex
 */
public class Treasure extends Feature {
	final ArrayList<Item> items;
	int gold;
	/**
	 * <code>null</code> if no key.
	 */
	public Key key = null;

	public Treasure(String thing, int i, int j, int goldp,
			ArrayList<javelin.model.item.Item> itemsp) {
		super(thing, i, j);
		items = itemsp;
		gold = goldp;
	}

	@Override
	public void activate() {
		if (key != null) {
			DungeonScreen.message("You have found a " + key
					+ ". Cross a portal to activate it!");
			Squad.active.receiveitem(key);
		} else if (items.isEmpty()) {
			DungeonScreen.message("Party receives $" + gold + "!");
			Squad.active.gold += gold;
		} else {
			String message =
					"Party receives " + items.get(0).name.toString() + "!";
			int quantity = items.size();
			if (quantity != 1) {
				message += " (x" + quantity + ")";
			}
			DungeonScreen.message(message);
			for (Item i : items) {
				Town.grab(i);
			}
		}
	}
}
