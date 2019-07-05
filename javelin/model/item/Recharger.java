package javelin.model.item;

import java.io.Serializable;

/**
 * Serializable component that helps {@link Item}s keep track of charges,
 * recharging, etc.
 *
 * @author alex
 */
public class Recharger implements Serializable{
	int capacity;
	int used=0;
	int recharging=0;
	double hourspercharge;

	/** Constructor. */
	public Recharger(int capacity){
		this.capacity=capacity;
		hourspercharge=24.0/capacity;
	}

	/** @return <code>true</code> if used all daily capacities. */
	public boolean isempty(){
		return used==capacity;
	}

	/** @return {@link #isempty()}, after using a charge. */
	public boolean discharge(){
		used+=1;
		return isempty();
	}

	/**
	 * @param hours Hours spent recharging.
	 * @return <code>true</code> if it has at least one usable charge.
	 */
	public boolean recharge(int hours){
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
}