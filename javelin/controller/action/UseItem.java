package javelin.controller.action;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Squad;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.IntroScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

public class UseItem extends Action {

	public static final Action SINGLETON = new UseItem();

	public UseItem() {
		super("Use item", "i");
	}

	@Override
	public String getDescriptiveName() {
		return "Use an item. Note that some items cannot be used when next to an opponent.";
	}

	public static void use() {
		final Combatant c = Game.hero().combatant;
		final Monster m = c.source;
		final Item item = queryforitemselection(c);
		if (item == null) {
			return;
		}
		c.ap += .5f;
		c.source = c.source.clone();
		if (item.use(c)) {
			Squad.active.equipment.get(m.toString()).remove(item);
		}
	}

	/**
	 * Asks player to choose an item.
	 * 
	 * @return null if for any reason the player has no items, cannot use any
	 *         right now, canceled... Otherwise the selected item.
	 */
	public static Item queryforitemselection(final Combatant c) {
		final List<Item> items = Squad.active.equipment
				.get(c.source.toString());
		for (final Item i : new ArrayList<Item>(items)) {
			if (!i.isusedinbattle()) {
				items.remove(i);
			}
		}
		if (items.isEmpty()) {
			Game.message("Isn't carrying items!", null, Delay.WAIT);
			return null;
		}
		final boolean threatened = BattleScreen.active.map.getState()
				.isEngaged(c);
		int i = 1;
		final TreeMap<Integer, Item> options = new TreeMap<Integer, Item>();
		String prompt = "Which item? (press q to quit)\n";
		for (final Item it : items) {
			if (threatened && !it.canuseneganged()) {
				continue;
			}
			options.put(i, it);
			prompt += "[" + i++ + "] " + it.name + "\n";
		}
		if (i == 1) {
			Game.message("Can't use any of these while threatened!!", null,
					Delay.WAIT);
			return null;
		}
		Game.message(prompt, null, Delay.NONE);
		try {
			final String string = IntroScreen.feedback().toString();
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
