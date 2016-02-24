package javelin.model.dungeon;

import javelin.controller.upgrade.Spell;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.screen.world.DungeonScreen;

/**
 * Heals hit points and spell uses.
 * 
 * @author alex
 */
public class Fountain extends Feature {

	public Fountain(String thing, int i, int j) {
		super(thing, i, j);
	}

	@Override
	public void activate() {
		for (Combatant c : Squad.active.members) {
			c.hp = c.maxhp;
			for (Spell s : c.spells) {
				s.used = 0;
			}
		}
		DungeonScreen.message("Party totally recovered!");
	}

}
