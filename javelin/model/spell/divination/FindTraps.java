package javelin.model.spell.divination;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.condition.FindingTraps;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * http://www.d20srd.org/srd/spells/findTraps.htm
 * 
 * Theoretically level 3 but we want this to work for 1 hour so it can be used
 * during a {@link Dungeon} exploration.
 * 
 * @author alex
 */
public class FindTraps extends Spell {
	/** Constructor. */
	public FindTraps() {
		super("Find traps", 3, SpellsFactor.ratespelllikeability(3),
				Realm.WIND);
		ispotion = true;
		castinbattle = false;
		castonallies = false;
		castoutofbattle = true;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		caster.addcondition(new FindingTraps(caster, casterlevel));
		String sign = caster.source.skills.search >= 0 ? "+" : "-";
		return caster + " now has search " + sign + caster.source.skills.search;
	}
}
