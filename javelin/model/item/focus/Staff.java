package javelin.model.item.focus;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * A more powerful type of equipable {@link Wand} that works like a {@link Rod}.
 *
 * @see Spell#isstaff
 * @author alex
 */
public class Staff extends Rod{
	/** Minimum {@link Spell#casterlevel}. */
	public static int MINLEVEL=Wand.MAXLEVEL+1;

	/** Here mostly to reuse code. */
	Wand wand;

	/** Constructor. */
	public Staff(Spell s){
		super("Staff",s,false);
		if(Javelin.DEBUG&&s.level<MINLEVEL)
			throw new InvalidParameterException(s.name);
		consumable=true;
		wand=new Wand(s,false);
		price=wand.price;
		register();
	}

	@Override
	public String canuse(Combatant c){
		return wand.decipher(c)?super.canuse(c):failure;
	}

	@Override
	public boolean use(Combatant user){
		if(!super.use(user)) return false;
		var result=wand.discharge();
		price=wand.price;
		return result;
	}

	@Override
	public String toString(){
		return super.toString()+" ["+wand.charges+"]";
	}

	@Override
	public String waste(float resourcesused,Combatant c,ArrayList<Item> bag){
		var before=wand.charges;
		wand.waste(resourcesused,c,bag);
		if(before==wand.charges) return null;
		var name=this.name.toLowerCase();
		if(wand.charges>0) return name+" ("+(before-wand.charges)+" times)";
		bag.remove(this);
		return "exhausted "+name;
	}

	@Override
	public Item randomize(){
		var clone=(Staff)super.randomize();
		clone.wand.randomize();
		clone.price=wand.price;
		return clone;
	}

	@Override
	public void expend(){
		if(wand.charges==0) super.expend();
	}
}
