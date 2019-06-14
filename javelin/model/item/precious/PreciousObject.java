package javelin.model.item.precious;

import java.util.List;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.old.RPG;

/**
 * Cosmetic items meant for sale. Can only be found as treasure.
 *
 * @author alex
 */
public abstract class PreciousObject extends Item{
	int dice;
	int sides;
	int multiplier;
	String type;

	/** Constructor. */
	protected PreciousObject(String name,int dice,int sides,int multiplier,
			String type){
		super(name,Math.round(dice*(sides+1)/2f*multiplier),true);
		this.type=type.toLowerCase();
		this.name+=" ("+type+")";
		usedinbattle=false;
		usedoutofbattle=true;
		this.dice=dice;
		this.sides=sides;
		this.multiplier=multiplier;
	}

	@Override
	public boolean usepeacefully(Combatant user){
		Javelin.message("This "+type+" could probably be sold for gold...",false);
		return false;
	}

	@Override
	public Item randomize(){
		var precious=(PreciousObject)super.randomize();
		precious.price=RPG.rolldice(dice,sides)*multiplier;
		return precious;
	}

	/** Prints out all precious items with initial and random prices. */
	public static void test(List<? extends Item> precious){
		for(var p:precious)
			System.out.println(
					p.name+" ($"+p.price+" median, $"+p.randomize().price+" random)");
	}
}