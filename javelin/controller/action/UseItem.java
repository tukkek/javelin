package javelin.controller.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.view.screen.InfoScreen;

/**
 * Activates an {@link Item} in battle.
 * 
 * @author alex
 */
public class UseItem extends Action {
	/** Unique instance of this class. */
	public static final Action SINGLETON = new UseItem();

	private UseItem() {
		super("Use item", "i");
	}

	@Override
	public String getDescriptiveName() {
		return "Use battle items";
	}

	@Override
	public boolean perform(Combatant active) {
		final Combatant c = active;
		final Item item = queryforitemselection(c, true);
		if (item == null) {
			return false;
		}
		c.ap += item.apcost;
		c.source = c.source.clone();
		if (item.use(c)) {
			Javelin.app.fight.getbag(c).remove(item);
		}
		return true;
	}

	/**
	 * Asks player to choose an item.
	 * 
	 * @return null if for any reason the player has no items, cannot use any
	 *         right now, canceled... Otherwise the selected item.
	 */
	public static Item queryforitemselection(final Combatant c,
			boolean validate) {
		final List<Item> items =
				(List<Item>) Javelin.app.fight.getbag(c).clone();
		if (items.isEmpty()) {
			Game.message("Isn't carrying battle items!", Delay.WAIT);
			return null;
		}
		if (validate) {
			for (final Item i : new ArrayList<Item>(items)) {
				if (!i.usedinbattle || i.canuse(c) != null) {
					items.remove(i);
				}
			}
		}
		if (items.isEmpty()) {
			Game.message("Can't use any of these items!", Delay.WAIT);
			return null;
		}
		Collections.sort(items, new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				return o1.name.compareTo(o2.name);
			}
		});
		final boolean threatened = Fight.state.isengaged(c);
		int i = 1;
		final TreeMap<Integer, Item> options = new TreeMap<Integer, Item>();
		String prompt = "Which item? (press q to quit)\n";
		for (final Item it : items) {
			if (threatened) {
				continue;
			}
			options.put(i, it);
			prompt += "[" + i++ + "] " + it + "\n";
		}
		if (i == 1) {
			Game.message("Can't use any of these while threatened!",
					Delay.WAIT);
			return null;
		}
		Game.message(prompt, Delay.NONE);
		try {
			final String string = InfoScreen.feedback().toString();
			final Item item = options.get(Integer.parseInt(string));
			Game.messagepanel.clear();
			return item;
		} catch (final NumberFormatException e) {
			Game.messagepanel.clear();
			return null;
		} catch (final IndexOutOfBoundsException e) {
			Game.messagepanel.clear();
			return null;
		}
	}

}
