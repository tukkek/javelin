package javelin.model.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javelin.controller.comparator.ItemsByPrice;

/**
 * Collection for {@link Item}. Does not allow for more than one item of the
 * same type.
 *
 * @author alex
 */
public class ItemSelection extends ArrayList<Item>{
	/** Constructor. */
	public ItemSelection(){
		super();
	}

	/** Constructor. */
	public ItemSelection(Collection<Item> selection){
		super(selection);
	}

	/**
	 * @return These items, sorted by price.
	 * @see ItemsByPrice
	 */
	public List<Item> sort(){
		sort(ItemsByPrice.SINGLETON);
		return this;
	}

	@Override
	public boolean add(Item i){
		return !contains(i)&&super.add(i);
	}
}
