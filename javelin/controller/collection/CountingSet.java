package javelin.controller.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Counts how many times an element is added to the set.
 *
 * TODO check if the order of {@link #getcount()} is being taken into
 * consideration - I don't think any ordering is guaranteed.
 *
 * @author alex
 */
public class CountingSet{
	/** Can be overriden to dictate ordering. */
	public Comparator<? super String> comparator=(a,b)->getcount(a)-getcount(b);
	/** Set to true if you don't want elements to be converted to lowercase. */
	public boolean casesensitive=false;
	TreeMap<String,Integer> map=new TreeMap<>();

	public CountingSet(){
		super();
	}

	public CountingSet(Collection<?> strings){
		addall(strings);
	}

	/**
	 * @param s Adds and counts this element (case-insensitive).
	 */
	public void add(String s){
		if(!casesensitive) s=s.toLowerCase();
		map.put(s,map.containsKey(s)?map.get(s)+1:1);
	}

	/**
	 * @return The entry set for the internal map.
	 * @see TreeMap#entrySet()
	 */
	public Set<Entry<String,Integer>> getcount(){
		return map.entrySet();
	}

	/**
	 * @return Current count for the given element.
	 */
	public int getcount(String s){
		if(!casesensitive) s=s.toLowerCase();
		return map.get(s);
	}

	public Set<String> getelements(){
		return map.keySet();
	}

	public List<String> getorderedelements(){
		ArrayList<String> elements=new ArrayList<>(getelements());
		elements.sort(comparator);
		return elements;
	}

	public List<String> getinvertedelements(){
		ArrayList<String> elements=new ArrayList<>(getelements());
		elements.sort((a,b)->-comparator.compare(a,b));
		return elements;
	}

	@Override
	public String toString(){
		String text="";
		for(String c:getorderedelements()){
			text+=c;
			int n=getcount(c);
			if(n>1) text+=" (x"+n+")";
			text+=", ";
		}
		return text.isEmpty()?"...":text.substring(0,text.length()-2);
	}

	/** @param pass Calls {@link #add(String)} after a {@link #toString()}. */
	public void add(Object o){
		add(String.valueOf(o));
	}

	/** @param items Consumed by {@link #add(Object)}. */
	public void addall(Collection<?> items){
		for(var i:items)
			add(i);
	}
}
