package javelin.model.item.consumable;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ContentSummary;
import javelin.controller.fight.Fight;
import javelin.model.item.Item;
import javelin.model.item.Recharger;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.conjuration.Summon;

/**
 * A one-use crystal that carries the essence of a creature (acts as a
 * {@link Summon} {@link Monster} item).
 *
 * @author alex
 */
public class Eidolon extends Item{
	/**
	 * TODO This has to be fine-tuned so as not to overwhelm the list of all
	 * {@link Item}s with a huge number of eidolons.
	 *
	 * @see ContentSummary
	 */
	static final List<Integer> VARIATIONS=List.of(0,1,2,3);

	Recharger charges=null;
	Monster m;

	/** Constructor. */
	public Eidolon(Summon s,int charges){
		super("Eidolon",s.level*s.casterlevel*(charges==0?50:2000/(5.0/charges)),
				true);
		m=Monster.get(s.monstername);
		if(charges==0)
			name="Minor "+name.toLowerCase();
		else{
			this.charges=new Recharger(charges);
			consumable=false;
		}
		name+=" ["+m.name.toLowerCase()+"]";
		provokesaoo=true;
		targeted=false;
		usedinbattle=true;
		usedoutofbattle=false;
		waste=true;
		if(charges==0) identified=false;
	}

	@Override
	public boolean use(Combatant user){
		var s=Summon.summon(m,user,Fight.state);
		Javelin.redraw();
		var message=user+" summons a creature: "+s.source.name+"!";
		Javelin.message(message,false);
		if(charges!=null&&charges.discharge()) usedinbattle=false;
		return true;
	}

	@Override
	public String toString(){
		if(!identified) return "Unidentified minor eidolon";
		var name=super.toString();
		return charges==null?name:name+" "+charges;
	}

	@Override
	public void refresh(int hours){
		super.refresh(hours);
		if(charges!=null&&charges.recharge(hours)) usedinbattle=true;
	}

	/** Dinamically initializes a proper amount of Eidolon instances. */
	@SuppressWarnings("unused")
	public static void generate(){
		for(var dailyuses:VARIATIONS)
			for(var s:Summon.select(Summon.SUMMONS,1))
				new Eidolon(s,dailyuses);
	}

	@Override
	public String waste(float resourcesused,Combatant c,ArrayList<Item> bag){
		if(charges==null) super.waste(resourcesused,c,bag);
		return charges.waste(c,this,resourcesused,bag);
	}

	@Override
	public Eidolon clone(){
		var e=(Eidolon)super.clone();
		e.m=m.clone();
		if(e.charges!=null) e.charges=charges.clone();
		return e;
	}
}
