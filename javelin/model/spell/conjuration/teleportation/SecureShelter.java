package javelin.model.spell.conjuration.teleportation;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Accommodations;
import javelin.model.world.location.town.Town;

/**
 * See the d20 SRD for more info.
 */
public class SecureShelter extends Spell {
	public SecureShelter() {
		super("Secure shelter", 4, SpellsFactor.ratespelllikeability(4),
				Realm.MAGICAL);
		castoutofbattle = true;
		isscroll = true;
	}

	@Override
	public boolean validate(Combatant caster, Combatant target) {
		return Dungeon.active == null;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		Town.rest(1, 8, Accommodations.LODGE);
		return null;
	}
}
