package javelin.controller.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.comparator.ItemsByName;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;

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
		final List<Item> items = (List<Item>) Javelin.app.fight.getbag(c)
				.clone();
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
		Collections.sort(items, ItemsByName.SINGLETON);
		final boolean threatened = Fight.state.isengaged(c);
		for (final Item it : new ArrayList<Item>(items)) {
			if (threatened && it.provokesaoo) {
				items.remove(it);
			}
		}
		if (items.isEmpty()) {
			Game.messagepanel.clear();
			Game.message("Can't use any of these while threatened!",
					Delay.WAIT);
			return null;
		}
		boolean fullscreen = items.size() > 4;
		String prompt = "Which item? (press q to quit)";
		int choice = Javelin.choose(prompt, items, fullscreen, false);
		Item i = choice >= 0 ? items.get(choice) : null;
		if (fullscreen) {
			Javelin.app.switchScreen(BattleScreen.active);
		}
		return i;
	}
}
