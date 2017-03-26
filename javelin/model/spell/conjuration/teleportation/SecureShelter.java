package javelin.model.spell.conjuration.teleportation;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Inn;

/**
 * See the d20 SRD for more info.
 */
public class SecureShelter extends Spell {
	public SecureShelter() {
		super("Secure shelter", 4,
				ChallengeRatingCalculator.ratespelllikeability(4), Realm.MAGIC);
		castoutofbattle = true;
		isscroll = true;
	}

	@Override
	public boolean validate(Combatant caster, Combatant target) {
		return Dungeon.active == null;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		Inn.rest(1, 8, Inn.LODGE);
		return null;
	}
}
