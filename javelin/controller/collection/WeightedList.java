package javelin.controller.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.old.RPG;

/**
 * A list where each element is added a certain number of times to provide a
 * distribution which can then be consumed with functions like
 * {@link RPG#pick(List)}.
 *
 * @see Collections#reverse(List)
 * @see Comparator#reversed()
 * @author alex
 */
public class WeightedList<K>{
	/** Result. */
	public List<K> distribution=new ArrayList<>();

	/** Constructor. */
	public WeightedList(){
		super();
	}

	/** Converts from existing items. */
	public WeightedList(Collection<K> c){
		for(var e:c)
			add(e);
	}

	/**
	 * @param element Added 1 time if it's the first element in the list, 2 if
	 *          it's the second, etc.
	 */
	public void add(K element){
		var entries=distribution.size()+1;
		for(var i=0;i<entries;i++)
			distribution.add(element);
	}
}