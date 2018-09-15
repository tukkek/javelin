package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Heals hit points and spell uses.
 *
 * @author alex
 */
public class Fountain extends Feature{
	static final String PROMPT="Do you want to drink from the fountain?\n"
			+"Press ENTER to drink or any other key to cancel...";

	/** Constructor. */
	public Fountain(){
		super("dungeonfountain");
	}

	@Override
	public boolean activate(){
		if(Javelin.prompt(PROMPT)!='\n') return false;
		for(Combatant c:Squad.active.members)
			heal(c);
		Javelin.message("Party fully recovered!",false);
		return true;
	}

	public static void heal(Combatant c){
		c.detox(c.source.poison);
		c.heal(c.maxhp,true);
		for(Spell s:c.spells)
			s.used=0;
	}
}
