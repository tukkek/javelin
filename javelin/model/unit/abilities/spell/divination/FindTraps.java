package javelin.model.unit.abilities.spell.divination;

import javelin.controller.challenge.CrCalculator;
import javelin.model.Realm;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;
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
	public class FindingTraps extends Condition {

		/**
		 * Constructor.
		 * 
		 * @param casterlevelp
		 */
		public FindingTraps(Combatant c, Integer casterlevelp) {
			super(Float.MAX_VALUE, c, Effect.NEUTRAL, "finding traps",
					casterlevelp, 1);
		}

		@Override
		public void start(Combatant c) {
			c.source.skills.search += 3;
		}

		@Override
		public void end(Combatant c) {
			c.source.skills.search -= 3;
		}
	}

	/** Constructor. */
	public FindTraps() {
		super("Find traps", 3,
				CrCalculator.ratespelllikeability(3), Realm.AIR);
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
