package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;

/**
 * Use an {@link Item} out of battle.
 *
 * @see Item#usedoutofbattle
 * @author alex
 */
public class UseItems extends WorldAction {
	static final ArrayList<Character> KEYS = SelectScreen.filterkeys("deq");

	public static boolean skiperror = false;

	/** Constructor. */
	public UseItems() {
		super("Inventory", new int[] {}, new String[] { "i" });
	}

	@Override
	public void perform(WorldScreen worldscreen) {
		skiperror = false;
		Squad.active.sort();
		while (true) {
			InfoScreen infoscreen = new InfoScreen("");
			String actions;
			actions = "Press number to use an item\n";
			actions += "Press d to drop an item\n";
			actions += "Press e to exchange an item\n";
			actions += "Press q to quit the inventory\n";
			ArrayList<Item> allitems = new ArrayList<>();
			String list = listitems(allitems, true);
			infoscreen.print(actions + list);
			if (executecommand(allitems, list, infoscreen)) {
				break;
			}
		}
		Javelin.app.switchScreen(JavelinApp.context);
	}

	boolean executecommand(ArrayList<Item> allitems, String list,
			InfoScreen infoscreen) {
		Javelin.app.switchScreen(infoscreen);
		Character input = InfoScreen.feedback();
		if (input == 'q') {
			return true;// leaves screen
		}
		if (input == 'e') {
			exchange(allitems, list, infoscreen);
			return false;
		}
		if (input == 'd') {
			Squad.active.equipment.remove(select(allitems, infoscreen));
			return false;
		}
		Item selected = select(allitems, input);
		return selected != null && selected.usedoutofbattle
				&& use(infoscreen, selected);
	}

	boolean use(InfoScreen infoscreen, Item selected) {
		Combatant target;
		if (selected instanceof Artifact) {
			target = findowner(selected);
		} else if (selected.targeted) {
			target = inputmember("Which member will use the "
					+ selected.toString().toLowerCase() + "?");
		} else {
			target = null;
		}
		if (!selected.usepeacefully(target)) {
			if (!skiperror) {
				String error = selected.describefailure();
				infoscreen.print(infoscreen.text + "\n\n" + error);
				InfoScreen.feedback();
			}
			return false;
		}
		if (selected.consumable) {
			selected.expend();
		}
		return true;
	}

	Item select(ArrayList<Item> allitems, Character input) {
		int index = KEYS.indexOf(input);
		return 0 <= index && index < allitems.size() ? allitems.get(index)
				: null;
	}

	void exchange(ArrayList<Item> allitems, String reequiptext,
			InfoScreen infoscreen) {
		Item i = select(allitems, infoscreen);
		if (i == null) {
			return;
		}
		Combatant owner = findowner(i);
		owner.unequip(i);
		Squad.active.equipment.get(owner).remove(i);
		Squad.active.equipment.get(selectmember(Squad.active.members, i,
				"Transfer " + i + " to who?")).add(i);
	}

	Item select(ArrayList<Item> allitems, InfoScreen infoscreen) {
		infoscreen.print(infoscreen.text + "\n\nSelect an item.");
		return select(allitems, InfoScreen.feedback());
	}

	Combatant findowner(Item selected) {
		for (Combatant c : Squad.active.members) {
			for (Item i : Squad.active.equipment.get(c)) {
				if (i == selected) {
					return c;
				}
			}
		}
		throw new RuntimeException("Item owner not found #useitems");
	}

	int count(Item it, List<Item> allitems) {
		int count = 0;
		for (Item i : allitems) {
			if (i.equals(it)) {
				count += 1;
			}
		}
		return count;
	}

	Combatant inputmember(String message) {
		ArrayList<Combatant> members = Squad.active.members;
		List<String> choices = members.stream()
				.map((member) -> member + " (" + member.getstatus() + ")")
				.collect(Collectors.toList());
		return members.get(Javelin.choose(message, choices, true, true));
	}

	/**
	 * @param allitems
	 *            Adds items to this list if not <code>null</code>.
	 * @param showkeys
	 *            If <code>true</code> will prepend each item with a key from
	 *            #KEYS.
	 * @return A textual listing.
	 */
	static public String listitems(ArrayList<Item> allitems, boolean showkeys) {
		String s = "";
		int i = 0;
		ArrayList<Combatant> members = filtermercenaries(Squad.active.members);
		for (int j = 0; j < members.size(); j++) {
			Combatant c = members.get(j);
			String output = "";
			if (!showkeys) {
				output += KEYS.get(j) + " - ";
			}
			output = c.toString();
			s += "\n";
			s += output + ":\n";
			boolean none = true;
			ArrayList<Item> bag = new ArrayList<>(
					Squad.active.equipment.get(c));
			Collections.sort(bag, new Comparator<Item>() {
				@Override
				public int compare(Item o1, Item o2) {
					return o1.name.compareTo(o2.name);
				}
			});
			for (Item it : bag) {
				if (allitems != null) {
					allitems.add(it);
				}
				if (showkeys) {
					Character key = i >= KEYS.size() ? '?' : KEYS.get(i);
					s += "  [" + key + "]";
				}
				s += " " + it.describe(c) + "\n";
				i += 1;
				none = false;
			}
			if (none) {
				s += "  carrying no items.\n";
			}
		}
		return s;
	}

	/**
	 * Tries to filter out mercenaries from the list. This is necessary because
	 * having 10 low-level mercenaries makes the equipment handling screens
	 * impossible to use. If squad is all mercenaries, will show it anyways.
	 *
	 * TODO this is a necessity due to the poor 1,0 series user interface. With
	 * 2.0+ should instead have a "show mercenaries" checkbox, allowing
	 * mercenaries to use items in this way.
	 */
	static ArrayList<Combatant> filtermercenaries(ArrayList<Combatant> all) {
		ArrayList<Combatant> members = new ArrayList<>(all.size());
		for (Combatant c : all) {
			if (!c.mercenary) {
				members.add(c);
			}
		}
		return members.isEmpty() ? all : members;
	}

	/**
	 * @param members
	 *            Eligible members.
	 * @param i
	 *            Item in question. See {@link Item#canuse(Combatant)}.
	 * @return Selected member.
	 */
	public static Combatant selectmember(ArrayList<Combatant> members, Item i,
			String text) {
		ArrayList<String> options = new ArrayList<>(members.size());
		for (Combatant c : members) {
			String option = c.toString();
			String invalid = i.canuse(c);
			if (invalid != null) {
				option += " (" + invalid + ")";
			}
			options.add(option);
		}

		return members.get(Javelin.choose(text, options, true, true));
	}
}
