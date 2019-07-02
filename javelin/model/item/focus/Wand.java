package javelin.model.item.focus;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.action.CastSpell;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.town.labor.basic.Shop;
import javelin.old.RPG;

/**
 * Generates both {@link Wand}s and {@link Staff}s. Any single-target,
 * non-{@link Touch} {@link Spell} can be a wand, even if it produces an area of
 * effect.
 *
 * Each wand has up to 50 charges and is spent when empty. Wands found through
 * exploration may have less {@link #charges} while wands bought brand-new on
 * {@link Shop}s should be {@link #FULL}.
 *
 * @see Spell#iswand
 * @author alex
 */
public class Wand extends Item{
	/** Wands should not be higher than level 4, subclasses may vary. */
	public static final int MAXLEVEL=4;
	static final int FULL=50;

	/** {@link #spell} uses left. */
	public int charges;

	int pricepercharge;
	int maxcharges;
	Spell spell;

	/**
	 * @return A name in the format Name of Spell, cleaning any common prefixes
	 *         like "ray of".
	 */
	public static String name(String name,Spell s){
		name=name+" of "+s.name.toLowerCase();
		if(name.contains("ray of ")) name=name.replace("ray of ","");
		return name;
	}

	/**
	 * Internal constructor for subclasses. Defers registration until the proper
	 * {@link #price} can be determined .
	 */
	Wand(Spell s,boolean checklevel){
		super(name("Wand",s),0,false);
		if(Javelin.DEBUG&&checklevel&(!s.iswand||s.level>MAXLEVEL))
			throw new InvalidParameterException();
		spell=s.clone();
		spell.provokeaoo=false;
		provokesaoo=false;
		usedinbattle=s.castinbattle;
		usedoutofbattle=s.castoutofbattle;
		apcost=s.apcost;
		pricepercharge=s.level*s.casterlevel*750/FULL+s.components;
		define(FULL);
		register();
	}

	public Wand(Spell s){
		this(s,true);
	}

	/**
	 * Offer a new charge/price for this item instead of always having a fixed
	 * price and full charges.
	 */
	void define(Integer charges){
		maxcharges=charges;
		this.charges=charges;
		price=pricepercharge*this.charges;
	}

	@Override
	public boolean use(Combatant user){
		if(!decipher(user)) return false;
		CastSpell.SINGLETON.cast(spell,user);
		return discharge();
	}

	/** @return <code>true</code> if empty. */
	protected boolean discharge(){
		charges-=1;
		price=pricepercharge*charges;
		return charges==0;
	}

	boolean decipher(Combatant user){
		failure=null;
		if(user.taketen(Skill.USEMAGICDEVICE)>=20||user.decipher(spell))
			return true;
		failure="Can't decipher";
		return false;
	}

	@Override
	public boolean usepeacefully(Combatant user){
		if(!decipher(user)) return false;
		spell.castpeacefully(user);
		return discharge();
	}

	@Override
	public String toString(){
		return name+" ["+charges+"]";
	}

	@Override
	public String canuse(Combatant c){
		return c.decipher(spell)?null:"Can't decipher";
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof Wand) return name.equals(((Wand)obj).name);
		return false;
	}

	@Override
	public String waste(float resourcesused,Combatant c,ArrayList<Item> bag){
		if(canuse(c)!=null||charges==0) return null;
		int used=Math.round(maxcharges*resourcesused);
		if(used==0) return null;
		used=Math.min(used,Math.max(1,maxcharges/5));
		if(used>charges) used=charges;
		for(var i=0;i<used;i++)
			discharge();
		if(charges>0) return name.toLowerCase()+" ("+used+" times)";
		bag.remove(this);
		return "exhausted "+name.toLowerCase();
	}

	@Override
	public Item randomize(){
		Wand clone=(Wand)super.randomize();
		clone.define(RPG.r(1,50));
		return clone;
	}
}
