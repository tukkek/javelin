package javelin.model.unit.abilities.spell.conjuration.teleportation;

import javelin.Javelin;
import javelin.controller.action.world.UseItems;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.exception.RepeatTurn;
import javelin.model.Realm;
import javelin.model.transport.Transport;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Brings you back to last visited town. See the d20 SRD for more info. Assumes
 * caster level 20 to be able to transport as many creatures as willed.
 * 
 * http://paizo.com/pathfinderRPG/prd/coreRulebook/spells/wordOfRecall.html
 */
public class WordOfRecall extends Spell {
	/** Constructor. */
	public WordOfRecall() {
		super("Word of recall", 6, CrCalculator.ratespelllikeability(6, 20),
				Realm.MAGIC);
		casterlevel = 20;
		castoutofbattle = true;
		isritual = true;
		isscroll = true;
	}

	@Override
	public boolean validate(Combatant caster, Combatant target) {
		if (Squad.active.lasttown == null) {
			return false;
		}
		UseItems.skiperror = true;
		String message = "Do you want to recall to " + Squad.active.lasttown
				+ "?\nPress ENTER to confirm or any other key to abort...";
		return Javelin.prompt(message, true) == '\n';
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant combatant) {
		teleport(Squad.active.lasttown.x, Squad.active.lasttown.y);
		return null;
	}

	static public void teleport(int x, int y) {
		Squad s = Squad.active;
		Transport t = s.transport;
		if (Dungeon.active != null) {
			Dungeon.active.leave();
			s.transport = null;
		} else if (t != null) {
			try {
				t.park();
			} catch (RepeatTurn e) {
				s.transport = null;
			}
		}
		s.x = x;
		s.y = y;
		s.displace();
		s.place();
	}
}
