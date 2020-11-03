package javelin.model.item.gear.rune;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.action.CastSpell;
import javelin.model.item.Item;
import javelin.model.item.Recharger;
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
	static final List<Condition> PREFIXES=new ArrayList<>();
	static final List<Spell> SUFFIXES=new ArrayList<>();

	static{
		NAMES.put(Slot.EYES,List.of("Goggles","Glasses"));
		NAMES.put(Slot.FEET,List.of("Boots","Shoes"));
		NAMES.put(Slot.FINGERS,List.of("Ring","Band","Circlet"));
		NAMES.put(Slot.HANDS,List.of("Gauntlet","Glove"));
		NAMES.put(Slot.HEAD,List.of("Helm","Hat","Mask"));
		NAMES.put(Slot.NECK,List.of("Pendant","Scarf","Locket"));
		NAMES.put(Slot.SHOULDERS,List.of("Cloak","Cape"));
		NAMES.put(Slot.TORSO,List.of("Fur","Poncho"));
		NAMES.put(Slot.WAIST,List.of("Belt","Kilt"));
		NAMES.put(Slot.SLOTLESS,List.of("Medal","Brooch"));
	}

	/** Creates item instances. */
	@SuppressWarnings("unused")
	public static void generate(){
		PREFIXES.addAll(Spell.SPELLS.stream().filter(s->s.isrune!=null)
				.map(s->s.isrune).collect(Collectors.toList()));
		SUFFIXES.addAll(Spell.SPELLS.stream().filter(s->!(s instanceof Summon))
				.collect(Collectors.toList()));
		new RuneGear(Slot.EYES,10);
		new RuneGear(Slot.FEET,20);
		new RuneGear(Slot.HANDS,10);
		new RuneGear(Slot.HEAD,30);
		new RuneGear(Slot.NECK,10);
		new RuneGear(Slot.FINGERS,5);
		new RuneGear(Slot.SHOULDERS,5);
		new RuneGear(Slot.TORSO,10);
		new RuneGear(Slot.WAIST,1);
		new RuneGear(Slot.SLOTLESS,5);
	}

	Condition prefix=null;
	int prefixprice=0;
	Spell suffix=null;
	int baseprice;
	Recharger charges=null;

	/** Constructor. */
	public RuneGear(Slot s,int baseprice){
		super(s+" gear",0,s);
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

	@Override
	public void refresh(int hours){
		super.refresh(hours);
		if(suffix!=null){
			charges.recharge(hours);
			if(charges.isempty()){
				usedinbattle=false;
				usedoutofbattle=false;
			}else{
				usedinbattle=suffix.castinbattle;
				usedoutofbattle=suffix.castoutofbattle;
			}
		}
	}

	void define(){
		var suffixprice=price(suffix);
		price=baseprice+prefixprice+suffixprice+Math.min(prefixprice,suffixprice)/2;
		assert price>baseprice;
		price*=slot==Slot.SLOTLESS?2:1.5;
		price=Javelin.round(price);
		if(suffix==null){
			usedinbattle=false;
			usedoutofbattle=false;
			charges=null;
		}
		refresh(0);
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
		charges=new Recharger(5);
		define();
	}

	@Override
	public String toString(){
		var s=getname(prefix,this,suffix);
		if(suffix!=null) s+=" "+charges;
		return s;
	}

	static String getname(Condition prefix,Item i,Spell suffix){
		var name=prefix==null?"":prefix.toString()+" ";
		name+=i.name.toLowerCase();
		if(suffix!=null) name+=" of "+suffix.name.toLowerCase();
		return Javelin.capitalize(name);
	}

	@Override
	protected void apply(Combatant c){
		if(prefix!=null) c.addcondition(prefix);
	}

	@Override
	protected void negate(Combatant c){
		if(prefix!=null) c.removecondition(prefix);
	}

	void discharge(){
		charges.discharge();
		refresh(0);
	}

	@Override
	public boolean use(Combatant user){
		if(charges.isempty()||!CastSpell.SINGLETON.cast(suffix,user)) return false;
		discharge();
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant c){
		if(charges.isempty()||!suffix.castpeacefully(c)) return false;
		discharge();
		return true;
	}

	@Override
	public RuneGear randomize(){
		assert owner==null;
		var g=(RuneGear)super.randomize();
		g.name=Javelin.DEBUG?NAMES.get(slot).get(0):RPG.pick(NAMES.get(slot));
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
		if(suffix!=null){
			g.suffix=suffix.clone();
			g.charges=charges.clone();
		}
		return g;
	}

	@Override
	public boolean canheal(Combatant c){
		return owner!=null&&suffix!=null&&!charges.isempty()&&suffix.canheal(c);
	}

	@Override
	public void heal(Combatant c){
		suffix.heal(c);
		discharge();
	}

	@Override
	public int getheals(){
		return charges.getleft();
	}
}
