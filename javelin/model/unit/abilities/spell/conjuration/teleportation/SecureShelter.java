package javelin.model.unit.abilities.spell.conjuration.teleportation;

import javelin.controller.challenge.CrCalculator;
import javelin.model.Realm;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.labor.basic.Lodge;

/**
 * See the d20 SRD for more info.
 */
public class SecureShelter extends Spell {
	public SecureShelter() {
		super("Secure shelter", 4,
				CrCalculator.ratespelllikeability(4), Realm.MAGIC);
		castoutofbattle = true;
		isscroll = true;
	}

	@Override
	public boolean validate(Combatant caster, Combatant target) {
		return Dungeon.active == null;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		Lodge.rest(1, 8, true, Lodge.LODGE);
		return null;
	}
}
