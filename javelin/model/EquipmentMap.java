package javelin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import tyrant.mikera.engine.RPG;

/**
 * Map of {@link Combatant#toString()} and a list of {@link Item}s.
 * 
 * TODO change key to int (combatant id)
 * 
 * @author alex
 */
public class EquipmentMap extends HashMap<Integer, ArrayList<Item>>
		implements Serializable {

	@Override
	public java.util.ArrayList<Item> get(final Object key) {
		if (!containsKey(key)) {
			put((Integer) key, new ArrayList<Item>());
		}
		return super.get(key);
	}

	public Item pop(Class<? extends Item> type) {
		for (final List<Item> items : values()) {
			for (final Item i : items) {
				if (type.isInstance(i)) {
					items.remove(i);
					return i;
				}
			}
		}
		return null;
	}

	public Item contains(Class<? extends Item> type) {
		for (final List<Item> items : values()) {
			for (final Item i : items) {
				if (type.isInstance(i)) {
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

	public void add(Item i, Squad s) {
		get(RPG.pick(s.members).id).add(i);
	}
}