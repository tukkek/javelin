package javelin.model.item;

import java.util.ArrayList;

import tyrant.mikera.engine.RPG;

/**
 * Collection for {@link Item}. Does not allow for more than one item of the
 * same type.
 * 
 * @author alex
 */
public class ItemSelection extends ArrayList<Item> {

	public ItemSelection(ItemSelection getselection) {
		super(getselection);
	}

	public ItemSelection() {
		super();
	}

	@Override
	public boolean add(Item element) {
		if (contains(element)) {
			return false;
		}
		return super.add(element);
	}

	public Item random() {
		return get(RPG.r(0, size() - 1));
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "";
		}
		String s = get(0).toString();
		for (int i = 1; i < size(); i++) {
			s += ", " + get(i);
		}
		return s.toLowerCase();
	}

	public boolean contains(Class<? extends Item> itemtype) {
		for (Item i : this) {
			if (itemtype.isInstance(i)) {
				return true;
			}
		}
		return false;
	}
}
