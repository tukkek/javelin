package javelin.model.item.relic;

import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.condition.Heroic;

/**
 * Recovers full hp and applies heroism to squad units for a day
 * 
 * @author alex
 */
public class Ankh extends Relic {
	/** Constructor. */
	public Ankh() {
		super("Ankh of Life", Realm.GOOD);
		usedinbattle = false;
		usedoutofbattle = true;
	}

	@Override
	protected boolean activate(Combatant user) {
		for (Combatant c : Squad.active.members) {
			c.heal(c.maxhp, true);
			c.addcondition(new Heroic(c, 20, 24));
		}
		return true;
	}

}
