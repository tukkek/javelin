package javelin.model.item.consumable;

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
 * TODO a major eidolon could be used once per day (actually Eidolon should be
 * the name of the major one)
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
	static final int PERLEVEL=5;

	Monster m;

	/** Constructor. */
	public Eidolon(Summon s){
		super("Eidolon",s.level*s.casterlevel*50,true);
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
		Javelin.redraw();
		var name=summoned.source.name;
		Javelin.message(user+" summons a creature: "+name+"!",false);
		return true;
	}

	/** Dinamically initializes a proper amount of Eidolon instances. */
	@SuppressWarnings("unused")
	public static void generate(){
		for(var s:Summon.select(Summon.SUMMONS,PERLEVEL))
			new Eidolon(s);
	}
}
