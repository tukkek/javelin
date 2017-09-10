package javelin.model.unit;

import java.util.ArrayList;
import java.util.Collection;

import javelin.model.Cloneable;

/**
 * Performs deep copy on {@link #clone()}.
 * 
 * @author alex
 */
public class CloneableList<K extends Cloneable> extends ArrayList<K> {

	public CloneableList() {
		super();
	}

	public CloneableList(int initialCapacity) {
		super(initialCapacity);
	}

	public CloneableList(Collection c) {
		super(c);
	}

	@Override
	public CloneableList<K> clone() {
		final CloneableList<K> clone = (CloneableList<K>) super.clone();
		for (int i = 0; i < size(); i++) {
			final K cloneable = get(i);
			clone.set(i, (K) cloneable.clone());
		}
		return clone;
	}
}