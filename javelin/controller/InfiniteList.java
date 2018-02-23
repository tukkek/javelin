package javelin.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class InfiniteList<T> {
	ArrayList<T> original = new ArrayList<T>();
	LinkedList<T> pool = new LinkedList<T>();
	boolean shuffle;

	public InfiniteList() {
		this(null);
	}

	public InfiniteList(Collection<T> original) {
		this(original, true);
	}

	public InfiniteList(Collection<T> original, boolean shuffle) {
		if (original != null) {
			this.original.addAll(original);
		}
		this.shuffle = shuffle;
	}

	public ArrayList<T> pop(int amount) {
		ArrayList<T> list = new ArrayList<T>(amount);
		for (int i = 0; i < amount; i++) {
			list.add(pop());
		}
		return list;
	}

	public T pop() {
		if (pool.isEmpty()) {
			if (shuffle) {
				Collections.shuffle(original);
			}
			pool.addAll(original);
		}
		return pool.pop();
	}

	public void add(List<T> list) {
		original.addAll(list);
	}

	public void remove(T choice) {
		original.remove(choice);
	}

	public void add(T e) {
		original.add(e);
	}
}
