package javelin.model.item.artifact;

import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.condition.Heroic;

/**
 * Recovers full hp and applies heroism to squad units for a day
 *
 * @author alex
 */
public class Ankh extends Artifact{
	/** Constructor. */
	public Ankh(){
		super("Ankh of Life");
		usedinbattle=false;
		usedoutofbattle=true;
	}

	@Override
	protected boolean activate(Combatant user){
		for(Combatant c:Squad.active.members){
			c.heal(c.maxhp,true);
			c.addcondition(new Heroic(null,24));
		}
		return true;
	}

}
