package javelin.model.world.location.dungeon;

import javelin.Javelin;
import javelin.controller.upgrade.Spell;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;

/**
 * Heals hit points and spell uses.
 * 
 * @author alex
 */
public class Fountain extends Feature {
	/** Constructor. */
	public Fountain(String thing, int i, int j) {
		super(thing, i, j, "dungeonfountain");
	}

	@Override
	public boolean activate() {
		for (Combatant c : Squad.active.members) {
			c.hp = c.maxhp;
			for (Spell s : c.spells) {
				s.used = 0;
			}
		}
		Javelin.message("Party totally recovered!", false);
		return true;
	}

}
