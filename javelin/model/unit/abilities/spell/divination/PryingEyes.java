package javelin.model.unit.abilities.spell.divination;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.wish.RevealFloor;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Reveals {@link Dungeon} map or nearby area on the WorldScreen.
 *
 * @author alex
 */
public class PryingEyes extends Spell {
	/** Constructor. */
	public PryingEyes() {
		super("Prying eyes", 5, ChallengeCalculator.ratespelllikeability(5),
				Realm.MAGIC);
		castoutofbattle = true;
		isscroll = true;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant combatant) {
		if (Dungeon.active == null) {
			Outpost.discover(Squad.active.x, Squad.active.y,
					Outpost.VISIONRANGE);
			return null;
		}
		RevealFloor.reveal(Dungeon.active);
		return null;
	}
}
