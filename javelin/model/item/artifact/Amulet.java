package javelin.model.item.artifact;

import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Condition;

/**
 * restores all used spells, recovers hp and eliminates conditions
 *
 * @author alex
 */
public class Amulet extends Artifact{

	/** Constructor. */
	public Amulet(Integer level){
		super("Amulet of Mana",level);
		usedinbattle=false;
		usedoutofbattle=true;
	}

	@Override
	protected boolean activate(Combatant user){
		for(Combatant c:Squad.active.members){
			for(Condition co:c.getconditions())
				c.removecondition(co);
			c.heal(c.maxhp,true);
			for(Spell s:c.spells)
				s.used=0;
		}
		return true;
	}

}
