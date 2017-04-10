package javelin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import tyrant.mikera.engine.RPG;

/**
 * Map of {@link Combatant#id} and a list of {@link Item}s. Also referred to as
 * a unit's "bag" in other parts of the code.
 *
 * TODO currently using *item in method names to differentiate between Map
 * methods. May want to enclose a map instead of extending one in the future.
 *
 * @author alex
 */
public class EquipmentMap extends HashMap<Integer, ArrayList<Item>>
		implements Serializable {

	@Override
	public ArrayList<Item> get(final Object key) {
		if (!containsKey(key)) {
			put((Integer) key, new ArrayList<Item>());
		}
		return super.get(key);
	}

	/**
	 * @return Any {@link Item} of this class, removed from the {@link Squad}'s
	 *         bags or <code>null</code> if not found.
	 */
	public Item popitem(Class<? extends Item> type, Squad s) {
		for (Combatant c : s.members) {
			ArrayList<Item> bag = get(c.id);
			for (final Item i : bag) {
				if (type.isInstance(i)) {
					bag.remove(i);
					c.unequip(i);
					return i;
				}
			}
		}
		return null;
	}

	public Item containsitem(Class<? extends Item> type) {
		for (final List<Item> items : values()) {
			for (final Item i : items) {
				if (type.isInstance(i)) {
					return i;
				}
			}
		}
		return null;
	}

	/**
	 * @return Any item equal to the given item, removed from the
	 *         {@link Squad}'s bags or <code>null</code> if not found.
	 */
	public Item popitem(Item type, Squad s) {
		for (Combatant c : s.members) {
			ArrayList<Item> bag = get(c.id);
			for (final Item i : bag) {
				if (type.equals(i)) {
					bag.remove(i);
					c.unequip(i);
					return i;
				}
			}
		}
		return null;
	}

	public void fill(Squad active) {
		for (Combatant c : active.members) {
			get(c.id);
		}
	}

	public void additem(Item i, Squad s) {
		get(RPG.pick(s.members).id).add(i);
	}

	/**
	 * TODO ideally should never to a "dirty" state
	 *
	 * @param squad
	 */
	public void clean(Squad squad) {
		keyloop: for (Integer key : new ArrayList<Integer>(keySet())) {
			for (Combatant c : squad.members) {
				if (c.id == key) {
					continue keyloop;
				}
			}
			remove(key);
		}
	}

	public int count() {
		int count = 0;
		for (ArrayList<Item> bag : values()) {
			count += bag.size();
		}
		return count;
	}

	/**
	 * @return The exact given instance, removed from the {@link Squad}'s bags
	 *         or <code>null</code> if not found.
	 */
	public Item removeitem(Item target, Squad s) {
		for (Combatant c : s.members) {
			ArrayList<Item> bag = get(c.id);
			for (final Item i : bag) {
				if (target == i) {
					bag.remove(i);
					c.unequip(i);
					return i;
				}
			}
		}
		return null;
	}
}