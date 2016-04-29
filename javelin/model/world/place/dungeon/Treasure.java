package javelin.model.world.place.dungeon;

import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.Key;
import javelin.model.world.Squad;
import javelin.model.world.place.town.Town;
import javelin.view.screen.DungeonScreen;

/**
 * Loot!
 * 
 * @author alex
 */
public class Treasure extends Feature {
	final ItemSelection items;
	int gold;
	/**
	 * <code>null</code> if no key.
	 */
	public Key key = null;

	public Treasure(String thing, int i, int j, int goldp,
			ItemSelection itemsp) {
		super(thing, i, j);
		items = itemsp;
		gold = goldp;
	}

	@Override
	public boolean activate() {
		if (key != null) {
			DungeonScreen
					.message("You have found a " + key.toString().toLowerCase()
							+ ". Cross a portal to activate it!");
			Town.grab(key);
		} else if (items.isEmpty()) {
			DungeonScreen.message("Party receives $" + gold + "!");
			Squad.active.gold += gold;
		} else {
			String message = "Party receives " + items + "!";
			int quantity = items.size();
			if (quantity != 1) {
				message += " (x" + quantity + ")";
			}
			DungeonScreen.message(message);
			for (Item i : items) {
				Town.grab(i);
			}
		}
		return true;
	}
}
