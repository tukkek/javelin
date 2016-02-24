package javelin.model.item;

import java.util.ArrayList;

/**
 * Collection for {@link Item}. Does not aloow for more than one item of the
 * same type.
 * 
 * @author alex
 */
public class ItemSelection extends ArrayList<Item> {

	@Override
	public boolean add(Item element) {
		if (contains(element)) {
			return false;
		}
		return super.add(element);
	}

}
