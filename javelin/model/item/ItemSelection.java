package javelin.model.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javelin.controller.comparator.ItemsByPrice;

/**
 * Collection for {@link Item}. Does not allow for more than one item of the
 * same type.
 *
 * @author alex
 */
public class ItemSelection extends HashSet<Item>{
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
		var sorted=new ArrayList<>(this);
		sorted.sort(ItemsByPrice.SINGLETON);
		return sorted;
	}
}
