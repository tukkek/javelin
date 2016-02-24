package javelin.controller;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Counts how many times an element is added to the set.
 * 
 * TODO check if the order of {@link #getCount()} is being taken into
 * consideration - I don't think any ordering is guaranteed.
 * 
 * @author alex
 */
public class CountingSet {
	private static final long serialVersionUID = 1L;

	TreeMap<String, Integer> tree = new TreeMap<String, Integer>();

	public void add(final String i) {
		final String i2 = i.toLowerCase();
		tree.put(i2, tree.containsKey(i2) ? tree.get(i2) + 1 : 1);
	}

	public Set<Entry<String, Integer>> getCount() {
		return tree.entrySet();
	}
}
