package javelin.model.item;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.action.CastSpell;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.skill.Skill;
import javelin.old.RPG;

/**
 * A wand is ideally a item that fires a ray. It has up to 50 charges and is
 * spent when empty.
 *
 * TODO an easy way to double the number of Wands (much needed as they're the
 * least common item type) would be also offering them as Rods - they would work
 * like wands but fully recharge every 24 hours, with 1d4+1 charges per day.
 *
 * @author alex
 */
public class Wand extends Item{
	public int charges;
	int baseprice;
	Spell spell;
	boolean shop=false;
	int maxcharges;

	/**
	 * Constructor.
	 *
	 * @param s Spell to be cast.
	 * @param upgradeset
	 */
	public Wand(Spell s){
		super("Wand of "+s.name.toLowerCase(),s.level*s.casterlevel*15+s.components,
				true);
		if(Javelin.DEBUG) assert s.iswand&&s.level>4;
		if(name.contains("ray of ")) name=name.replace("ray of ","");
		spell=s.clone();
		spell.provokeaoo=false;
		provokesaoo=false;
		baseprice=price;
		recharge(null);
		usedinbattle=s.castinbattle;
		usedoutofbattle=s.castoutofbattle;
		apcost=s.apcost;
	}

	/**
	 * Offer a new charge/price for this item instead of always having a fixed
	 * price and full charges.
	 */
	void recharge(Integer charges){
		maxcharges=charges==null?50:charges;
		this.charges=maxcharges;
		price=baseprice*this.charges;
	}

	@Override
	public boolean use(Combatant user){
		if(!decipher(user)) return false;
		CastSpell.SINGLETON.cast(spell,user);
		return discharge();
	}

	boolean discharge(){
		charges-=1;
		price=baseprice*charges;
		return charges==0;
	}

	boolean decipher(Combatant user){
		failure=null;
		if(user.taketen(Skill.USEMAGICDEVICE)>=20||user.decipher(spell))
			return true;
		failure="Cannot currently decipher this spell.";
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
		return c.decipher(spell)?null:"can't decipher";
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof Wand) return name.equals(((Wand)obj).name);
		return false;
	}

	@Override
	public String waste(float resourcesused,Combatant c,ArrayList<Item> bag){
		if(canuse(c)!=null) return null;
		int used=Math.round(maxcharges*resourcesused);
		if(used==0) return null;
		used=Math.min(used,Math.max(1,maxcharges/5));
		charges-=used;
		if(charges<=0){
			bag.remove(this);
			return "exhausted "+name.toLowerCase();
		}
		return name.toLowerCase()+" ("+used+" times)";
	}

	@Override
	public Item randomize(){
		Wand clone=(Wand)super.randomize();
		clone.recharge(RPG.r(1,50));
		return clone;
	}
}
