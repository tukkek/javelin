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
public class Fountain extends Feature {
	/** Constructor. */
	public Fountain() {
		super("dungeonfountain");
	}

	@Override
	public boolean activate() {
		for (Combatant c : Squad.active.members) {
			heal(c);
		}
		Javelin.message("Party totally recovered!", false);
		return true;
	}

	public static void heal(Combatant c) {
		c.detox(c.source.poison);
		c.heal(c.maxhp, true);
		for (Spell s : c.spells) {
			s.used = 0;
		}
	}

}
