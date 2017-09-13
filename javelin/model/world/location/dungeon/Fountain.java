package javelin.model.world.location.dungeon;

import javelin.Javelin;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;

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
			c.heal(c.maxhp, true);
			for (Spell s : c.spells) {
				s.used = 0;
			}
		}
		Javelin.message("Party totally recovered!", false);
		return true;
	}

}
