package javelin.model.item.potion;

import java.io.Serializable;
import java.util.List;

import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Flaks are like {@link Potion}s that can be {@link #refresh(int)}-ed.
 *
 * @author alex
 */
public class Flask extends Potion{
	/** How many types of Flasks to generate, {@link #capacity}-wise. */
	public static final List<Integer> VARIATIONS=List.of(5);

	class Recharger implements Serializable{
		int capacity;
		int used=0;
		int recharging=0;
		double hourspercharge;

		public Recharger(int capacity){
			this.capacity=capacity;
			hourspercharge=24.0/capacity;
		}

		public boolean isempty(){
			return used==capacity;
		}

		public void discharge(){
			used+=1;
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

		public int getleft(){
			return capacity-used;
		}
	}

	Recharger recharger;

	/** Constructor. */
	public Flask(Spell s,int capacity){
		super("Flask",s,
				s.level*s.casterlevel*2000/(5/capacity)+s.components*capacity,true);
		recharger=new Recharger(capacity);
		consumable=false;
	}

	void quaff(){
		recharger.discharge();
		if(recharger.isempty()){
			usedinbattle=false;
			usedoutofbattle=false;
		}
	}

	@Override
	public boolean use(Combatant user){
		if(!super.use(user)) return false;
		quaff();
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant user){
		if(!super.usepeacefully(user)) return false;
		quaff();
		return true;
	}

	@Override
	public void refresh(int hours){
		super.refresh(hours);
		if(recharger.recharge(hours)){
			usedinbattle=spell.castinbattle;
			usedoutofbattle=spell.castoutofbattle;
		}
	}

	@Override
	public String toString(){
		var left=recharger.getleft();
		return super.toString()+" ["+(left==0?"empty":left)+"]";
	}
}
