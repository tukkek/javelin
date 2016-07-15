package javelin.model.item.relic;

import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.condition.Condition;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;

/**
 * restores all used spells, recovers hp and eliminates conditions
 * 
 * @author alex
 */
public class Amulet extends Relic {

	/** Constructor. */
	public Amulet() {
		super("Amulet of Mana", Realm.MAGIC);
		usedinbattle = false;
		usedoutofbattle = true;
	}

	@Override
	protected boolean activate(Combatant user) {
		for (Combatant c : Squad.active.members) {
			for (Condition co : c.getconditions()) {
				c.removecondition(co);
			}
			c.hp = c.maxhp;
			for (Spell s : c.spells) {
				s.used = 0;
			}
		}
		return true;
	}

}
