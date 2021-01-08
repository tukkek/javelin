package javelin.model.item.focus;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.content.action.CastSpell;
import javelin.model.item.Item;
import javelin.model.item.Recharger;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.town.labor.basic.Shop;
import javelin.old.RPG;

/**
 * Any single-target, non-{@link Touch} {@link Spell} can be a Wand, even if it
 * produces an area of effect. Each wand has up to 50 charges and is spent when
 * empty. Wands found through exploration may have less {@link #charges} while
 * wands bought brand-new on {@link Shop}s should be {@link #FULL}.
 *
 * TODO use {@link Recharger} (but without {@link #refresh(int)}). It will need
 * a bit of enhancing since it doesn't currently know about non
 * max-charge/renewable items. In partiular
 * {@link Recharger#waste(Combatant, Item, float, java.util.List)} has been
 * improved, and can also be used by {@link Staff} since it relies on
 * {@link Wand#waste(float, Combatant, ArrayList)}.
 *
 * @see Spell#iswand
 * @author alex
 */
public class Wand extends Item{
	/**
	 * Wand {@link Spell}s should not be higher than level 4.
	 *
	 * @see Staff
	 */
	public static final int MAXLEVEL=4;
	static final int FULL=50;

	double pricepercharge;
	int maxcharges;
	int charges;
	Spell spell;

	/**
	 * @return A name in the format "Name of spell", removing any unnecessary
	 *         element (such as "Ray of").
	 */
	public static String name(String name,Spell s){
		name=name+" of "+s.name.toLowerCase();
		if(name.contains("ray of ")) name=name.replace("ray of ","");
		return name;
	}

	/**
	 * @param checklevel If <code>false</code>, will ignore {@link #MAXLEVEL}
	 *          errors.
	 */
	public Wand(Spell s,boolean checklevel){
		super(name("Wand",s),0,true);
		if(Javelin.DEBUG&&checklevel&(!s.iswand||s.level>MAXLEVEL))
			throw new InvalidParameterException();
		spell=s.clone();
		spell.provokeaoo=false;
		provokesaoo=false;
		usedinbattle=s.castinbattle;
		usedoutofbattle=s.castoutofbattle;
		apcost=s.apcost;
		pricepercharge=s.level*s.casterlevel*750.0/FULL+s.components;
		define(FULL);
	}

	/** Constructor. */
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
		setprice(pricepercharge*this.charges);
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
		setprice(pricepercharge*charges);
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
		var clone=(Wand)super.randomize();
		clone.define(RPG.r(1,50));
		return clone;
	}
}
