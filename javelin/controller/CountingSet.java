package javelin.controller;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class CountingSet {
	private static final long serialVersionUID = 1L;

	TreeMap<String, Integer> tree = new TreeMap<String, Integer>();

	public void add(final String i) {
		final String i2 = i.toLowerCase();
		tree.put(i2, tree.containsKey(i2) ? tree.get(i2) + 1 : 1);
	}

	public Set<Entry<String, Integer>> getCount() {
		return tree.entrySet();
		/*
		 * final TreeMap<Integer, String> orderedErrors = new TreeMap<Integer,
		 * String>();
		 * 
		 * for (final Entry<String, Integer> e : tree.entrySet()) {
		 * orderedErrors.put(e.getValue(), e.getKey()); }
		 * 
		 * return orderedErrors.descendingMap().entrySet();
		 */
	}
}
