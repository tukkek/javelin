package javelin.model.item.consumable;

import java.util.List;

import javelin.Javelin;
import javelin.controller.ContentSummary;
import javelin.controller.fight.Fight;
import javelin.model.item.Item;
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
	 * This has to be fine-tuned so as not to overwhelm the list of all
	 * {@link Item}s with a huge number of eidolons.
	 *
	 * @see ContentSummary
	 */
	static final List<Integer> VARIATIONS=List.of(1,2,3,4,5);

	Monster m;
	int charges;
	int used=0;

	/** Constructor. */
	public Eidolon(Summon s,int charges){
		super("Eidolon",s.level*s.casterlevel*2000/(5/charges),true);
		this.charges=charges;
		m=Monster.get(s.monstername);
		name+=" ["+m.name.toLowerCase()+"]";
		consumable=true;
		provokesaoo=true;
		targeted=false;
		usedinbattle=true;
		usedoutofbattle=false;
		waste=true;
	}

	@Override
	public boolean use(Combatant user){
		var summoned=Summon.summon(m,user,Fight.state);
		used+=1;
		if(used==0) usedinbattle=false;
		Javelin.redraw();
		var name=summoned.source.name;
		Javelin.message(user+" summons a creature: "+name+"!",false);
		return true;
	}

	@Override
	public String toString(){
		var left=charges-used;
		return super.toString()+" ["+(left==0?"empty":left)+"]";
	}

	@Override
	public void refresh(int hours){
		super.refresh(hours);
	}

	/** Dinamically initializes a proper amount of Eidolon instances. */
	@SuppressWarnings("unused")
	public static void generate(){
		for(var dailyuses:VARIATIONS)
			for(var s:Summon.select(Summon.SUMMONS,1))
				new Eidolon(s,dailyuses);
	}
}
