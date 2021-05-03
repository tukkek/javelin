package javelin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.old.RPG;

/**
 * Map of {@link Combatant#id} and a list of {@link Item}s. Also referred to as
 * a unit's "bag" in other parts of the code.
 *
 * TODO currently using *item in method names to differentiate between Map
 * methods. May want to enclose a map instead of extending one in the future.
 *
 * @author alex
 */
public class Equipment implements Serializable{
	HashMap<Integer,ArrayList<Item>> equipment=new HashMap<>();
	Squad squad;

	public Equipment(Squad s){
		squad=s;
	}

	public ArrayList<Item> get(Combatant c){
		if(!equipment.containsKey(c.id)) equipment.put(c.id,new ArrayList<Item>());
		return equipment.get(c.id);
	}

	/**
	 * @return Any {@link Item} of this class, removed from the {@link Squad}'s
	 *         bags or <code>null</code> if not found.
	 */
	public Item pop(Class<? extends Item> type){
		for(Combatant c:squad.members){
			ArrayList<Item> bag=get(c);
			for(final Item i:bag)
				if(type.isInstance(i)){
					bag.remove(i);
					c.unequip(i);
					return i;
				}
		}
		return null;
	}

	public <K extends Item> K get(Class<K> type){
		for(final List<Item> items:equipment.values())
			for(final Item i:items)
				if(type.isInstance(i)) return (K)i;
		return null;
	}

	/**
	 * @return Any item equal to the given item, removed from the {@link Squad}'s
	 *         bags or <code>null</code> if not found.
	 */
	public Item pop(Item type){
		for(Combatant c:squad.members){
			ArrayList<Item> bag=get(c);
			for(final Item i:bag)
				if(type.equals(i)){
					bag.remove(i);
					c.unequip(i);
					return i;
				}
		}
		return null;
	}

	public void fill(){
		for(Combatant c:squad.members)
			get(c);
	}

	/** @param i Adds to a random bag, with no user interaction. */
	public void add(Item i){
		i.identified=squad.identify(i);
		get(RPG.pick(squad.members)).add(i);
	}

	/** TODO ideally should never be in a "dirty" state */
	public void clean(){
		var current=squad.members.stream().map(c->c.id).collect(Collectors.toSet());
		for(var id:new ArrayList<>(equipment.keySet()))
			if(!current.contains(id)) equipment.remove(id);
	}

	/**
	 * @return Total number of {@link Item}s this squad possesses.
	 */
	public int count(){
		int count=0;
		for(ArrayList<Item> bag:equipment.values())
			count+=bag.size();
		return count;
	}

	/**
	 * @return The exact given instance, removed from the {@link Squad}'s bags or
	 *         <code>null</code> if not found.
	 */
	public Item remove(Item target){
		for(Combatant c:squad.members){
			var bag=get(c);
			for(int i=0;i<bag.size();i++){
				var holding=bag.get(i);
				if(target==holding){
					c.unequip(holding);
					bag.remove(i);
					return holding;
				}
			}
		}
		return null;
	}

	public void remove(Combatant c){
		equipment.remove(c.id);
	}

	public void put(Combatant c,ArrayList<Item> items){
		for(var i:items)
			i.identified=squad.identify(i);
		equipment.put(c.id,items);
	}

	public Collection<ArrayList<Item>> values(){
		return equipment.values();
	}

	/**
	 * @param item Will check bags for {@link Item#equals(Object)}
	 * @return <code>null</code> if item was not found or the internal instance,
	 *         if so.
	 */
	public <K extends Item> K get(K item){
		for(ArrayList<Item> bag:equipment.values())
			for(final Item i:bag)
				if(i.equals(item)) return (K)i;
		return null;
	}

	public <K extends Item> List<K> getall(Class<K> type){
		ArrayList<K> list=new ArrayList<>();
		for(ArrayList<Item> bag:equipment.values())
			for(Item i:bag)
				if(type.isInstance(i)) list.add((K)i);
		return list;
	}

	/** @return All Items in all bags. */
	public List<Item> getall(){
		clean();
		var all=new ArrayList<Item>();
		for(var bag:values())
			all.addAll(bag);
		return all;
	}

	/** Calls {@link Item#refresh(int)} for all items in the bag. */
	public void refresh(int hours){
		getall().forEach(i->i.refresh(hours));
	}

	/**
	 * @return <code>true</code> if can pay the given amoutn of items.
	 *         <code>false</code> if can't, in which case no items will be
	 *         removed.
	 */
	public boolean pay(Class<? extends Item> item,long amount){
		if(getall(item).size()<amount) return false;
		for(var i=0;i<amount;i++)
			pop(item);
		return true;
	}

	/** Removes all items. */
	public void clear(){
		equipment.clear();
	}

	/**
	 * @return The {@link Combatant} that owns this exact instace, or
	 *         <code>null</code> of no one does.
	 */
	public Combatant getowner(Item item){
		for(var m:squad)
			for(var i:get(m))
				if(i==item) return m;
		return null;
	}
}