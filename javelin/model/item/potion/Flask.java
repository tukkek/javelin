package javelin.model.item.potion;

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

	double charging=0;
	int capacity;
	int used=0;

	/** Constructor. */
	public Flask(Spell s,int capacity){
		super("Flask",s,
				s.level*s.casterlevel*2000/(5/capacity)+s.components*capacity,true);
		this.capacity=capacity;
		consumable=false;
	}

	void quaff(){
		used+=1;
		if(used==capacity){
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
		/**
		 * could be easily refactored to Recharger class for reuse, probably with
		 * Item#recharge() to inform of a new charge (default throws
		 * UnsupportedOperationException.
		 */
		charging+=hours;
		var hourspercharge=24.0/capacity;
		while(charging>=hourspercharge&&used>0){
			used-=1;
			charging-=hourspercharge;
			usedinbattle=spell.castinbattle;
			usedoutofbattle=spell.castoutofbattle;
		}
		if(used==0) charging=0;
	}

	@Override
	public String toString(){
		var left=capacity-used;
		return super.toString()+" ["+(left==0?"empty":left)+"]";
	}
}
