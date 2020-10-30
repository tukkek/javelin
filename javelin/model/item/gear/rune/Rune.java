package javelin.model.item.gear.rune;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Condition;

/**
 * Consumable that adds a {@link RuneGear#prefix} or {@link RuneGear#suffix}.
 *
 * @author alex
 */
public class Rune extends Item{
	static final String PROMPT="Select an item to apply %s to.\n"
			+"(Note that selecting an already enchanted item may override an existing prefix or suffix)";
	Condition prefix;
	Spell suffix;

	/** Registers all instances. */
	@SuppressWarnings("unused")
	public static void generate(){
		for(var p:RuneGear.PREFIXES)
			new Rune(p,null);
		for(var s:RuneGear.SUFFIXES)
			new Rune(null,s);
	}

	/** Constructor. */
	public Rune(Condition prefix,Spell suffix){
		super("rune",0,true);
		if(prefix!=null&&suffix!=null)
			throw new InvalidParameterException("Runes should have a single affix");
		this.prefix=prefix;
		this.suffix=suffix;
		name=RuneGear.getname(prefix,this,suffix);
		price=prefix!=null?RuneGear.price(prefix):RuneGear.price(suffix);
		targeted=false;
		usedinbattle=false;
	}

	@Override
	public boolean usepeacefully(Combatant user){
		var gear=Squad.active.equipment.getall(RuneGear.class);
		if(gear.isEmpty()){
			failure="You don't currently possess any gear that can be augmented with a rune...";
			return false;
		}
		var prompt=String.format(PROMPT,this);
		var names=new ArrayList<String>(gear.size());
		for(var g:gear){
			var name=g.toString();
			if(g.owner!=null) name+=String.format(" (equipped by %s)",g.owner);
			names.add(name);
		}
		var choice=Javelin.choose(prompt,names,true,false);
		if(choice<0) return false;
		var enchant=gear.get(choice);
		if(prefix!=null)
			enchant.set(prefix);
		else
			enchant.set(suffix);
		return true;
	}
}
