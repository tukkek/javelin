package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.town.labor.religious.Shrine;

/**
 * Heals hit points and spell uses.
 *
 * TODO could also use a {@link Shrine} dungeon features. Spell#isritual should
 * make it simple enough.
 *
 * @author alex
 */
public class Fountain extends Feature{
	static final String PROMPT="Do you want to drink from the fountain?\n"
			+"Press ENTER to drink or any other key to cancel...";

	/** Constructor. */
	public Fountain(){
		super("fountain");
	}

	@Override
	public boolean activate(){
		if(Javelin.prompt(PROMPT)!='\n') return false;
		for(Combatant c:Squad.active.members)
			heal(c);
		Squad.active.equipment.refresh(24);
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
