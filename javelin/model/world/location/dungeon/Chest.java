package javelin.model.world.location.dungeon;

import java.util.List;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Squad;
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
	public ItemSelection items = new ItemSelection();
	public int gold = 0;

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
	public Chest(int x, int y) {
		super("chest", x, y, "dungeonchest");
	}

	@Override
	public boolean activate() {
		if (items.isEmpty()) {
			if (gold < 1) {
				gold = 1;
			}
			String message = "Party receives $"
					+ PurchaseScreen.formatcost(gold) + "!";
			Javelin.message(message, false);
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

	/**
	 * @param goldpool
	 *            Value to be added in gold or {@link Item}s.
	 * @param x
	 *            {@link Dungeon} coordinate.
	 * @param y
	 *            {@link Dungeon} coordinate.
	 * @return A {@link Dungeon} chest.
	 */
	public static Chest create(int goldpool, int x, int y) {
		Chest c = new Chest(x, y);
		c.gold = goldpool;
		if (RPG.r(1, 2) == 1) {// 50% are gold and 50% are item
			List<Item> all = Item.randomize(Item.ALL);
			for (int i = all.size() - 1; i >= 0; i--) {
				Item item = all.get(i);
				if (item.price < c.gold * .9) {
					break;
				}
				if (item.price < c.gold) {
					c.gold -= item.price;
					c.items.add(item);
					break;
				}
			}
		}
		return c;
	}

	public void setspecial() {
		avatarfile = "dungeonchestspecial";
	}
}
