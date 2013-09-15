package javelin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.model.item.Item;

/**
 * Map of m.toString() and a list of Items.
 * 
 * TODO change key to int (combatant id)
 * 
 * @author alex
 */
public class EquipmentMap extends HashMap<String, List<Item>> implements
		Serializable {
	@Override
	public java.util.List<Item> get(final Object key) {
		if (!containsKey(key)) {
			put((String) key, new ArrayList<Item>());
		}
		return super.get(key);
	}
}