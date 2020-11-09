package javelin.model.item.potion;

import java.util.List;

import javelin.model.item.Item;
import javelin.model.item.Recharger;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Flaks are {@link Potion}s that can be {@link #refresh(int)}-ed.
 *
 * @author alex
 */
public class Flask extends Potion{
	/**
	 * How many types of Flasks to generate, {@link #capacity}-wise.
	 *
	 * TODO eventually want 1-5 enabled, but cannot overwhelmed other {@link Item}
	 * types.
	 */
	public static final List<Integer> VARIATIONS=List.of(5);

	Recharger charges;

	/** Constructor. */
	public Flask(Spell s,int capacity){
		super("Flask",s,0,true);
		price=s.level*s.casterlevel*400*capacity;
		price+=capacity>=5?s.components*100:s.components*50;
		charges=new Recharger(capacity);
		consumable=false;
		waste=true;
	}

	void quaff(){
		if(charges.discharge()){
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
		if(charges.recharge(hours)){
			usedinbattle=spell.castinbattle;
			usedoutofbattle=spell.castoutofbattle;
		}
	}

	@Override
	public String toString(){
		return super.toString()+" "+charges;
	}

	@Override
	public Flask clone(){
		var f=(Flask)super.clone();
		f.charges=charges.clone();
		return f;
	}

	@Override
	public boolean canheal(Combatant c){
		return !charges.isempty()&&spell.canheal(c);
	}

	@Override
	public void heal(Combatant c){
		spell.heal(c);
		charges.discharge();
	}

	@Override
	public int getheals(){
		return charges.getleft();
	}
}
