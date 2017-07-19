package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
	static final String KEYS = "1234567890abcdfghijklmnoprstuvxwyz";

	/** Constructor. */
	public UseItems() {
		super("Inventory", new int[] {}, new String[] { "i" });
	}

	@Override
	public void perform(final WorldScreen worldscreen) {
		Squad.active.sort();
		while (true) {
			final ArrayList<Item> allitems = new ArrayList<Item>();
			final InfoScreen infoscreen = new InfoScreen("");
			String actions = "";
			actions += "Press number to use an item";
			actions += "\nPress e to exchange an item";
			actions += "\nPress q to quit the inventory";
			actions += "\n";
			final String list = listitems(allitems, true);
			infoscreen.print(actions + list);
			if (command(allitems, list, infoscreen)) {
				break;
			}
		}
		Javelin.app.switchScreen(JavelinApp.context);
	}

	boolean command(final ArrayList<Item> allitems, final String list,
			final InfoScreen infoscreen) {
		Javelin.app.switchScreen(infoscreen);
		final Character input = InfoScreen.feedback();
		if (input == 'q') {
			return true;// leaves screen
		}
		if (input == 'e') {
			exchange(allitems, list, infoscreen);
			return false;
		}
		Item selected = select(allitems, input);
		if (selected == null || !selected.usedoutofbattle) {
			return false;
		}
		Combatant target = selected instanceof Artifact ? findowner(selected)
				: inputmember("Which member will use the "
						+ selected.toString().toLowerCase() + "?");
		if (!selected.usepeacefully(target)) {
			infoscreen.print(
					infoscreen.text + "\n\n" + selected.describefailure());
			InfoScreen.feedback();
		} else if (selected.consumable) {
			selected.expend();
		}
		return selected.consumable;
	}

	Item select(final ArrayList<Item> allitems, final Character input) {
		Item selected = null;
		int index = SelectScreen.convertnumericselection(input,
				KEYS.toCharArray());
		if (0 <= index && index < allitems.size()) {
			selected = allitems.get(index);
		}
		return selected;
	}

	void exchange(final ArrayList<Item> allitems, final String reequiptext,
			final InfoScreen infoscreen) {
		infoscreen.print(infoscreen.text + "\n\nSelect an item.");
		Item selected = select(allitems, InfoScreen.feedback());
		if (selected == null) {
			return;
		}
		Combatant owner = findowner(selected);
		owner.unequip(selected);
		Squad.active.equipment.get(owner.id).remove(selected);
		Squad.active.equipment.get(selectmember(Squad.active.members, selected,
				"Transfer " + selected + " to who?").id).add(selected);
	}

	Combatant findowner(Item selected) {
		for (Combatant bag : Squad.active.members) {
			for (Item i : Squad.active.equipment.get(bag.id)) {
				if (i == selected) {
					return bag;
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

	Combatant inputmember(final String message) {
		return Squad.active.members
				.get(Javelin.choose(message, Squad.active.members, true, true));
	}

	/**
	 * @param allitems
	 *            Adds items to this list if not <code>null</code>.
	 * @param showkeys
	 *            If <code>true</code> will prepend each item with a key from
	 *            {@link SelectScreen#KEYS}.
	 * @return A textual listing.
	 */
	static public String listitems(final ArrayList<Item> allitems,
			boolean showkeys) {
		String s = "";
		int i = 0;
		ArrayList<Combatant> members = Squad.active.members;
		for (int j = 0; j < members.size(); j++) {
			final Combatant c = members.get(j);
			String output = "";
			if (!showkeys) {
				output += SelectScreen.KEYS[j] + " - ";
			}
			output = c.toString();
			s += "\n";
			s += output + ":\n";
			boolean none = true;
			ArrayList<Item> bag = new ArrayList<Item>(
					Squad.active.equipment.get(c.id));
			Collections.sort(bag, new Comparator<Item>() {
				@Override
				public int compare(Item o1, Item o2) {
					return o1.toString().compareTo(o2.toString());
				}
			});
			for (final Item it : bag) {
				if (allitems != null) {
					allitems.add(it);
				}
				if (showkeys) {
					s += "  [" + SelectScreen.KEYS[i] + "]";
				}
				s += describeitem(it, c);
				i += 1;
				none = false;
			}
			if (none) {
				s += "  carrying no items.\n";
			}
		}
		return s;
	}

	static String describeitem(final Item it, final Combatant c) {
		String s = " " + it.name;
		String useerror = it.canuse(c);
		if (useerror != null) {
			s += " (" + useerror + ")";
		} else if (c.equipped.contains(it)) {
			s += " (equipped)";
		}
		return s + "\n";
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
		ArrayList<String> options = new ArrayList<String>(members.size());
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
