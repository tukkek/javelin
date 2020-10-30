package javelin.model.item.gear.rune;

import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.CastSpell;
import javelin.model.item.gear.Gear;
import javelin.model.unit.Combatant;
import javelin.model.unit.Slot;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Condition;
import javelin.old.RPG;

/**
 * A "blank" piece of {@link Gear} that can have a {@link #prefix}, a
 * {@link #suffix} or both. A prefix is a passive condition that is permanent
 * and non-cumulative while the item is equipped. A suffix is an at-will spell
 * that can be cast by using the item.
 *
 * @author alex
 */
public class RuneGear extends Gear{
	static final HashMap<Slot,List<String>> NAMES=new HashMap<>();
	static final HashMap<Slot,List<Integer>> PRICE=new HashMap<>();

	/** Creates item instances. */
	@SuppressWarnings("unused")
	public static void generate(){
		new RuneGear("Tabard",5,Slot.ARMOR);
		new RuneGear("Vest",30,Slot.ARMOR);
		new RuneGear("Glasses",5,Slot.EYES);
		new RuneGear("Goggles",10,Slot.EYES);
		new RuneGear("Boots",20,Slot.FEET);
		new RuneGear("Shoes",5,Slot.FEET);
		new RuneGear("Glove",5,Slot.HANDS);
		new RuneGear("Gauntlet",10,Slot.HANDS);
		new RuneGear("Hat",30,Slot.HEAD);
		new RuneGear("Mask",30,Slot.HEAD);
		new RuneGear("Scarf",5,Slot.NECK);
		new RuneGear("Pendant",10,Slot.NECK);
		new RuneGear("Locket",10,Slot.NECK);
		new RuneGear("Bandy",5,Slot.RING);
		new RuneGear("Ring",5,Slot.RING);
		new RuneGear("Cape",5,Slot.SHOULDERS);
		new RuneGear("Cloak",5,Slot.SHOULDERS);
		new RuneGear("Fur",10,Slot.TORSO);
		new RuneGear("Poncho",1,Slot.TORSO);
		new RuneGear("Kilt",1,Slot.WAIST);
		new RuneGear("Belt",1,Slot.WAIST);
		new RuneGear("Brooch",5,Slot.SLOTLESS);
		new RuneGear("Medal",5,Slot.SLOTLESS);
	}

	Condition prefix=null;
	Spell suffix=null;
	int baseprice;

	/** Constructor. */
	public RuneGear(String name,int baseprice,Slot s){
		super(name,0,s);
		this.baseprice=baseprice;
		waste=false;
		usedinbattle=false;
		usedoutofbattle=false;
		provokesaoo=false;
	}

	void define(){
		var prefixprice=prefix==null?0:0; //TODO
		var suffixprice=suffix==null?0:suffix.casterlevel*suffix.level*2000;
		price=baseprice+prefixprice+suffixprice+Math.min(prefixprice,suffixprice)/2;
		assert price>baseprice;
		price*=slot==Slot.SLOTLESS?2:1.5;
		price=Javelin.round(price);
		if(owner==null){
			usedinbattle=false;
			usedoutofbattle=false;
		}else{
			usedinbattle=suffix.castinbattle;
			usedoutofbattle=suffix.castoutofbattle;
		}
	}

	/** @param prefix Add this prefix. */
	public void set(Condition prefix){
		if(owner!=null&&this.prefix!=null) negate(owner);
		this.prefix=prefix.clone();
		define();
		if(owner!=null) apply(owner);
	}

	/** @param suffix Adds this suffix. */
	public void set(Spell suffix){
		this.suffix=suffix.clone();
		this.suffix.provokeaoo=false;
		define();
	}

	@Override
	public String toString(){
		var name=prefix==null?"":prefix.toString()+" ";
		name+=super.toString().toLowerCase();
		if(suffix!=null) name+=" of "+suffix.name.toLowerCase();
		return Javelin.capitalize(name);
	}

	@Override
	protected void apply(Combatant c){
		//TODO prefix
	}

	@Override
	protected void negate(Combatant c){
		//TODO prefix
	}

	@Override
	public boolean equip(Combatant c){
		var e=super.equip(c);
		define();
		return e;
	}

	@Override
	public boolean use(Combatant user){
		return CastSpell.SINGLETON.cast(suffix,user);
	}

	@Override
	public boolean usepeacefully(Combatant c){
		return suffix.castpeacefully(c);
	}

	@Override
	public RuneGear randomize(){
		assert owner==null;
		var g=(RuneGear)super.randomize();
		g.prefix=null;
		g.suffix=null;
		var hasprefix=false;
		var hassuffix=false;
		while(!hasprefix&&!hassuffix){
			hasprefix=RPG.chancein(2);
			hasprefix=false;//TODO
			hassuffix=RPG.chancein(2);
		}
		if(hasprefix) g.set((Condition)null); //TODO
		if(hassuffix) g.set(RPG.pick(Spell.SPELLS));
		return g;
	}

	@Override
	public RuneGear clone(){
		var g=(RuneGear)super.clone();
		if(prefix!=null) g.prefix=prefix.clone();
		if(suffix!=null) g.suffix=suffix.clone();
		return g;
	}
}
