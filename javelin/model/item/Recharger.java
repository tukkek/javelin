package javelin.model.item;

import java.io.Serializable;
import java.util.List;

import javelin.model.unit.Combatant;

/**
 * Serializable component that helps {@link Item}s keep track of charges,
 * recharging, etc.
 *
 * @see Item#refresh(int)
 * @author alex
 */
public class Recharger implements Serializable,Cloneable{
	/** Number of charges when full. */
	public int capacity;

	int used=0;
	double recharging=0;

	/** Constructor. */
	public Recharger(int capacity){
		this.capacity=capacity;
	}

	/** @return <code>true</code> if used all daily capacities. */
	public boolean isempty(){
		return used==capacity;
	}

	/** @return {@link #isempty()}, after spending charges. */
	public boolean discharge(int charges){
		used+=charges;
		if(used>capacity) used=capacity;
		return isempty();
	}

	/** @return As {@link #discharge(int)} with only 1 spent charge. */
	public boolean discharge(){
		return discharge(1);
	}

	/**
	 * @param hours Hours spent recharging.
	 * @return <code>true</code> if it has at least one usable charge.
	 */
	public boolean recharge(int hours){
		if(hours==0) return !isempty();
		recharging+=hours;
		var hourspercharge=24.0/capacity;
		while(recharging>=hourspercharge&&used>0){
			used-=1;
			recharging-=hourspercharge;
		}
		if(used==0) recharging=0;
		return used<capacity;
	}

	/** @return Charges left until {@link #isempty()}. */
	public int getleft(){
		return capacity-used;
	}

	@Override
	public String toString(){
		return "["+getleft()+"/"+capacity+"]";
	}

	/** @see Item#waste() */
	public String waste(Combatant c,Item i,float resourcesused,List<Item> bag){
		if(i.canuse(c)!=null||isempty()) return null;
		int used=Math.round(capacity*resourcesused);
		if(used==0) return null;
		if(used>capacity/5) used=capacity/5;
		var left=getleft();
		if(used>left) used=left;
		if(used<1) used=1;
		var name=i.name.toLowerCase();
		if(!discharge(used)) return name+" ("+used+" times)";
		bag.remove(i);
		return "exhausted "+name;
	}

	@Override
	public Recharger clone(){
		try{
			return (Recharger)super.clone();
		}catch(CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
	}
}