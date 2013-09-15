package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.db.StateManager;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.town.ShoppingScreen;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.tyrant.InfoScreen;

public class UseItems extends WorldAction {
	public UseItems() {
		super("Inventory", new int[] {}, new String[] { "i" });
	}

	@Override
	public void perform(final WorldScreen worldscreen) {
		final ArrayList<Item> allitems = new ArrayList<Item>();
		final String list = listitems(allitems);
		final InfoScreen infoscreen = new InfoScreen(list);
		String actions = "";
		actions += "\nPress number to use an item";
		actions += "\nPress d to distribute items";
		actions += "\nPress q to quit the inventory";
		final String string = list + actions;
		print(infoscreen, string);
		while (true) {
			Javelin.app.switchScreen(infoscreen);
			final Character input = IntroScreen.feedback();
			if (input == 'd') {
				redistributeinventory(allitems, list, infoscreen);
			} else if (input == 'q') {
				// leaves screen
			} else if (Character.isDigit(input)) {
				ShoppingScreen.listactivemembers();
				int index = Integer.parseInt(Character.toString(input)) - 1;
				final Item selected = allitems.get(index);
				final Combatant m = inputmember(infoscreen, infoscreen.text
						+ "\n\nWhich member will use this item?");
				if (selected.usepeacefully(m)) {
					List<Item> items = null;
					spend: for (final Combatant owner : Squad.active.members) {
						items = Squad.active.equipment.get(owner.toString());
						/*
						 * needs extra loop to catch actual instance not just
						 * any item of the same type
						 */
						for (Item used : new ArrayList<Item>(items)) {
							if (used == selected) {
								items.remove(used);
								break spend;
							}
						}
					}
					StateManager.save();
				} else {
					print(infoscreen, infoscreen.text
							+ "\n\nCan only be used in battle.");
					IntroScreen.feedback();
				}
			} else {
				continue;
			}
			Javelin.app.switchScreen(worldscreen);
			break;
		}
	}

	public void print(final InfoScreen infoscreen, final String string) {
		infoscreen.text = string;
		Javelin.app.switchScreen(infoscreen);
	}

	public void redistributeinventory(final ArrayList<Item> allitems,
			final String reequiptext, final InfoScreen infoscreen) {
		Squad.active.equipment.clear();
		final String original = infoscreen.text;
		int i = 1;
		Collections.sort(allitems, new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				return o1.price - o2.price;
			}
		});
		for (final Item it : allitems) {
			final Combatant member = inputmember(infoscreen,
					original + "\n\nWhich squad member will carry the "
							+ it.name.toLowerCase() + " [" + i + "]?");
			Squad.active.equipment.get(member.toString()).add(it);
			i += 1;
		}
		print(infoscreen, original);
	}

	public Combatant inputmember(final InfoScreen infoscreen,
			final String message) {
		print(infoscreen, message + "\n\n" + ShoppingScreen.listactivemembers());
		while (true) {
			try {
				return Squad.active.members.get(Integer.parseInt(IntroScreen
						.feedback().toString()) - 1);
			} catch (final NumberFormatException e) {
				continue;
			} catch (final IndexOutOfBoundsException e) {
				continue;
			}
		}
	}

	public String listitems(final ArrayList<Item> allitems) {
		String reequiptext = "Your items:\n\n";
		int i = 1;
		for (final Combatant c : Squad.active.members) {
			String monster = c.toString();
			for (final Item it : Squad.active.equipment.get(monster)) {
				allitems.add(it);
				reequiptext += "[" + i + "] " + it.name + " (with " + monster
						+ ")\n";
				i += 1;
			}
		}
		return reequiptext;
	}
}
