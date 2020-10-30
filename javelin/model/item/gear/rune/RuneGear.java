package javelin.model.item.gear.rune;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.action.CastSpell;
import javelin.model.item.Item;
import javelin.model.item.gear.Gear;
import javelin.model.unit.Combatant;
import javelin.model.unit.Slot;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
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
	static final List<Condition> PREFIXES=new ArrayList<>();
	static final List<Spell> SUFFIXES=new ArrayList<>();

	/** Creates item instances. */
	@SuppressWarnings("unused")
	public static void generate(){
		PREFIXES.addAll(Spell.SPELLS.stream().filter(s->s.isrune!=null)
				.map(s->s.isrune).collect(Collectors.toList()));
		SUFFIXES.addAll(Spell.SPELLS.stream().filter(s->!(s instanceof Summon))
				.collect(Collectors.toList()));
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
		new RuneGear("Band",5,Slot.RING);
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
	int prefixprice=0;
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

	static int price(Condition prefix){
		var p=prefix.casterlevel*prefix.spelllevel*2000;
		if(prefix.longterm==null)
			p*=4;
		else if(prefix.longterm==0)
			p*=2;
		else if(prefix.longterm<=2)
			p*=1.5;
		else if(prefix.longterm>=24) p/=2;
		return p;
	}

	static int price(Spell suffix){
		return suffix==null?0:suffix.casterlevel*suffix.level*2000;
	}

	void define(){
		var suffixprice=price(suffix);
		price=baseprice+prefixprice+suffixprice+Math.min(prefixprice,suffixprice)/2;
		assert price>baseprice;
		price*=slot==Slot.SLOTLESS?2:1.5;
		price=Javelin.round(price);
		if(owner==null||suffix==null){
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
		if(prefix==null){
			this.prefix=null;
			prefixprice=0;
			return;
		}
		this.prefix=prefix.clone();
		prefixprice=price(prefix);
		define();
		this.prefix.expireat=Float.MAX_VALUE;
		this.prefix.longterm=Integer.MAX_VALUE;
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
		return getname(prefix,this,suffix);
	}

	static String getname(Condition prefix,Item i,Spell suffix){
		var name=prefix==null?"":prefix.toString()+" ";
		name+=i.name.toLowerCase();
		if(suffix!=null) name+=" of "+suffix.name.toLowerCase();
		return Javelin.capitalize(name);
	}

	@Override
	protected void apply(Combatant c){
		c.addcondition(prefix);
	}

	@Override
	protected void negate(Combatant c){
		c.removecondition(prefix);
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
		g.prefixprice=0;
		g.suffix=null;
		var hasprefix=false;
		var hassuffix=false;
		while(!hasprefix&&!hassuffix){
			hasprefix=RPG.chancein(2);
			hassuffix=RPG.chancein(2);
		}
		if(hasprefix) g.set(RPG.pick(PREFIXES));
		if(hassuffix) g.set(RPG.pick(SUFFIXES));
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
