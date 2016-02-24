package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.screen.town.SelectScreen;
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
		final String list = listitems(allitems, true);
		final InfoScreen infoscreen = new InfoScreen(list);
		String actions = "\n";
		actions += "\nPress number to use an item";
		actions += "\nPress d to distribute items";
		actions += "\nPress q to quit the inventory";
		infoscreen.print(list + actions);
		command(allitems, list, infoscreen);
	}

	public void command(final ArrayList<Item> allitems, final String list,
			final InfoScreen infoscreen) {
		while (true) {
			Javelin.app.switchScreen(infoscreen);
			final Character input = InfoScreen.feedback();
			if (input == 'd') {
				redistributeinventory(allitems, list, infoscreen);
			} else if (input == 'q') {
				// leaves screen
			} else {
				ShoppingScreen.listactivemembers();
				int index = SelectScreen.convertselectionkey(input);
				if (index >= allitems.size() || index == -1) {
					continue;
				}
				final Item selected = allitems.get(index);
				if (selected
						.usepeacefully(inputmember(infoscreen, infoscreen.text
								+ "\n\nWhich member will use this item?"))) {
					selected.expend();
				} else {
					infoscreen.print(infoscreen.text + "\n\n"
							+ selected.describefailure());
					InfoScreen.feedback();
				}
			}
			Javelin.app.switchScreen(JavelinApp.context);
			break;
		}
	}

	public void redistributeinventory(final ArrayList<Item> allitems,
			final String reequiptext, final InfoScreen infoscreen) {
		Squad.active.equipment.clear();
		final String original = infoscreen.text;
		Collections.sort(allitems, new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				int delta = o2.price - o1.price;
				if (delta == 0) {
					return o1.name.compareTo(o2.name);
				}
				return delta;
			}
		});
		final int nitems = allitems.size();
		for (int i = 0; i < nitems; i++) {
			final Item it = allitems.get(i);
			final Combatant member = inputmember(infoscreen,
					original + "\n\nWhich squad member will carry the "
							+ it.name.toLowerCase() + " ("
							+ count(it, allitems.subList(i, nitems)) + ")?");
			Squad.active.equipment.get(member.id).add(it);
			// i += 1;
		}
		infoscreen.print(original);
	}

	private int count(Item it, List<Item> allitems) {
		int count = 0;
		for (Item i : allitems) {
			if (i.equals(it)) {
				count += 1;
			}
		}
		return count;
	}

	public Combatant inputmember(final InfoScreen infoscreen,
			final String message) {
		infoscreen.print(message + "\n\n" + ShoppingScreen.listactivemembers());
		while (true) {
			try {
				return Squad.active.members.get(
						Integer.parseInt(InfoScreen.feedback().toString()) - 1);
			} catch (final NumberFormatException e) {
				continue;
			} catch (final IndexOutOfBoundsException e) {
				continue;
			}
		}
	}

	static public String listitems(final ArrayList<Item> allitems,
			boolean showkeys) {
		String s = "";
		int i = 0;
		for (final Combatant c : Squad.active.members) {
			String monster = c.toString();
			s += "\n" + monster + ":\n";
			boolean none = true;
			for (final Item it : Squad.active.equipment.get(c.id)) {
				if (allitems != null) {
					allitems.add(it);
				}
				if (showkeys) {
					s += "  [" + SelectScreen.SELECTIONKEYS[i] + "]";
				}
				s += " " + it.name + "\n";
				i += 1;
				none = false;
			}
			if (none) {
				s += "   carrying no items.\n";
			}
		}
		return s;
	}
}
