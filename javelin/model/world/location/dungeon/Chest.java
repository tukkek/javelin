package javelin.model.world.location.dungeon;

import java.util.List;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.Key;
import javelin.model.unit.Squad;
import javelin.model.world.location.unique.Haxor;
import javelin.view.screen.town.PurchaseScreen;
import tyrant.mikera.engine.RPG;

/**
 * Loot!
 * 
 * @author alex
 */
public class Chest extends Feature {
	/**
	 * TODO it's OK for Chests go only give a single item
	 */
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
	 * @param gold
	 *            Value to be added in gold or {@link Item}s.
	 * @param x
	 *            {@link Dungeon} coordinate.
	 * @param y
	 *            {@link Dungeon} coordinate.
	 * @return A {@link Dungeon} chest.
	 */
	public static Chest create(int gold, int x, int y) {
		ItemSelection items = new ItemSelection();
		if (RPG.r(1, 2) == 1) {// 50% are gold and 50% are item
			List<Item> all = Item.randomize(Item.ALL);
			for (int i = all.size() - 1; i >= 0; i--) {
				Item item = all.get(i);
				if (item.price < gold * .9) {
					break;
				}
				if (item.price < gold) {
					gold -= item.price;
					items.add(item);
					break;
				}
			}
		}
		return new Chest("chest", x, y, gold, items);
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
