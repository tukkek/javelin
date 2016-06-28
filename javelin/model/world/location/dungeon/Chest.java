package javelin.model.world.location.dungeon;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.Key;
import javelin.model.unit.Squad;
import javelin.model.world.location.Portal;
import javelin.view.screen.town.PurchaseScreen;
import tyrant.mikera.engine.Thing;

/**
 * Loot!
 * 
 * @author alex
 */
public class Chest extends Feature {
	final ItemSelection items;
	int gold;
	/**
	 * <code>null</code> if no key.
	 */
	public Key key = null;

	/**
	 * @param visual
	 *            Name of this {@link Thing}.
	 * @param x
	 *            Location.
	 * @param y
	 *            Location.
	 * @param goldp
	 *            Gold loot. See {@link Squad#gold}.
	 * @param itemsp
	 *            {@link Item} loot.
	 */
	public Chest(String visual, int x, int y, int goldp, ItemSelection itemsp) {
		super(visual, x, y, "dungeonchest");
		items = itemsp;
		gold = goldp;
	}

	@Override
	public boolean activate() {
		if (key != null) {
			Javelin.message("You have found a " + key.toString().toLowerCase()
					+ ". Cross a portal to activate it!", true);
			key.grab();
			Portal.opensafe();
		} else if (items.isEmpty()) {
			Javelin.message(
					"Party receives $" + PurchaseScreen.formatcost(gold) + "!",
					false);
			Squad.active.gold += gold;
		} else {
			String message = "Party receives " + items + "!";
			int quantity = items.size();
			if (quantity != 1) {
				message += " (x" + quantity + ")";
			}
			Javelin.message(message, false);
			for (Item i : items) {
				i.grab();
			}
		}
		return true;
	}
}
