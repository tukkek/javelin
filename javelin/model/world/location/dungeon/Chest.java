package javelin.model.world.location.dungeon;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.Key;
import javelin.model.unit.Squad;
import javelin.model.world.location.Portal;
import javelin.model.world.location.unique.Haxor;
import javelin.view.screen.town.PurchaseScreen;

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
	/** See {@link Haxor#rubies}. */
	public int rubies = 0;

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
			String message = "You have found: " + key.toString().toLowerCase()
					+ "!";
			Javelin.message(message, true);
			key.grab();
			Portal.opensafe();
		} else if (!items.isEmpty()) {
			String message = "Party receives " + items + "!";
			int quantity = items.size();
			if (quantity != 1) {
				message += " (x" + quantity + ")";
			}
			Javelin.message(message, false);
			for (Item i : items) {
				i.grab();
			}
		} else if (rubies > 0) {
			Javelin.message(takeruby(rubies), false);
		} else {
			String message = "Party receives $"
					+ PurchaseScreen.formatcost(gold) + "!";
			Javelin.message(message, false);
			Squad.active.gold += gold;
		}
		return true;
	}

	/**
	 * @param rubies
	 *            quantity of rubies to add.
	 * @return
	 * @return a message about getting a ruby. Note that the message assumes
	 *         only 1 is gained.
	 * @see Haxor#rubies
	 */
	public static String takeruby(int rubies) {
		Haxor.singleton.rubies += rubies;
		return "You find a ruby! Haxor might appreciate these...";
	}
}
