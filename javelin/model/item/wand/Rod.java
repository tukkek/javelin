package javelin.model.item.wand;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Works like a Wand but anyone can use and has a number of charges per day.
 *
 * TODO at some point may make wands level <=4 and rods >4. for now leave both
 * to reach a proper number of item instances between both.
 *
 * TODO check potential level>4 spells
 *
 * TODO tune VARIATIONS
 *
 * @author alex
 */
public class Rod extends Wand{
	/**
	 * How many variations to generate for each {@link Spell} (variations defined
	 * by {@link Wand#charges} per day.
	 *
	 * Note that an item daily uses shouldn't be higher than 5, by design.
	 */
	public static final int VARIATIONS=4;

	int recharging=0;

	/** Constructor. */
	public Rod(Spell s,int charges){
		super("Rod",s);
		consumable=false;
		var multiplier=s.continuous;
		if(multiplier==-1){
			if(Javelin.DEBUG)
				throw new RuntimeException("Item#continuous not defined: "+s.name);
			multiplier=1; //quick fix for user-side bad cases
		}
		pricepercharge=s.level*s.casterlevel*2000+s.components*50*multiplier;
		maxcharges=charges;
		recharge(charges);
		register();
	}

	@Override
	void recharge(Integer charges){
		super.recharge(maxcharges);
	}

	@Override
	boolean decipher(Combatant user){
		return true;
	}

	@Override
	public String canuse(Combatant c){
		return null;
	}

	@Override
	public Item randomize(){
		return clone();
	}

	@Override
	public boolean equals(Object obj){
		return super.equals(obj)&&((Wand)obj).maxcharges==maxcharges;
	}

	@Override
	protected boolean discharge(){
		super.discharge();
		if(charges>0) return false;
		usedinbattle=false;
		usedoutofbattle=false;
		return true;
	}

	@Override
	public void refresh(int hours){
		recharging+=hours;
		var timetorecharge=24.0/maxcharges;
		while(recharging>=timetorecharge&&charges<maxcharges){
			charges+=1;
			recharging-=timetorecharge;
		}
		if(charges>0){
			if(charges==maxcharges) recharging=0;
			usedinbattle=spell.castinbattle;
			usedoutofbattle=spell.castoutofbattle;
		}
	}

	@Override
	public String toString(){
		return name+" ["+charges+"/"+maxcharges+"]";
	}
}
