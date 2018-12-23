package javelin.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Taking a collection of items during or after construction, will serve those
 * items until exhausted, at which point will refill itself with the same items
 * - thus providing a seemingly inexhaustible stream of content.
 *
 * Internally, this class contains two pools: the content pool (the original
 * items, to be served indefinitely) and the current pool (or temporary pool,
 * which is the current state of items to be delivered). Whenever an item is
 * requested and the current pool is empty, it is first transparently refilled
 * with the items from the content pool.
 *
 * @author alex
 */
public class InfiniteList<T> implements Serializable{
	ArrayList<T> content=new ArrayList<>(0);
	LinkedList<T> current=new LinkedList<>();
	boolean shuffle;

	/**
	 * @param original Initial content pool.
	 * @param shuffle If <code>true</code>, will randomize the pool's order
	 *          whenever a refill happens.
	 */
	public InfiniteList(Collection<T> original,boolean shuffle){
		if(original!=null) this.content.addAll(original);
		this.shuffle=shuffle;
	}

	/** Removes and returns a given amount of items from the current pool. */
	public ArrayList<T> pop(int amount){
		ArrayList<T> list=new ArrayList<>(amount);
		for(int i=0;i<amount;i++)
			list.add(pop());
		return list;
	}

	void refill(){
		if(isempty()){
			if(shuffle) Collections.shuffle(content);
			current.addAll(content);
		}
	}

	/** @return <code>true</code> if the current pool is empty. */
	public boolean isempty(){
		return current.isEmpty();
	}

	/** @return First item, removed from the current pool. */
	public T pop(){
		refill();
		return current.pop();
	}

	/** @param list Items are added to the content pool. */
	public void addcontent(Collection<T> list){
		content.addAll(list);
	}

	/** * @param choice Item is removed from the content pool. */
	public void removecontent(T choice){
		content.remove(choice);
	}

	/** * @param e Added to the content pool. */
	public void addcontent(T e){
		content.add(e);
	}

	@Override
	public String toString(){
		return "Current pool: "+current;
	}

	/** @return How many elements define the content set. */
	public int getcontentsize(){
		return content.size();
	}

	/** @return A copy of {@link #content}. */
	public List<T> getcontent(){
		return new ArrayList<>(content);
	}
}
