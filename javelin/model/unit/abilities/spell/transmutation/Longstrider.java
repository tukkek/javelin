package javelin.model.unit.abilities.spell.transmutation;

import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * http://www.d20srd.org/srd/spells/longstrider.htm
 * 
 * Only castable in battle but will live up to 1 hour if inside a
 * {@link Dungeon}. Can't cast outside because it will seem useless for now.
 * TODO allow leveling up spells
 * 
 * @author alex
 */
public class Longstrider extends Spell {
	public class Strider extends Condition {
		public Strider(Combatant c, Integer casterlevelp) {
			super(Float.MAX_VALUE, c, Effect.POSITIVE, "striding", casterlevelp,
					1);
		}

		@Override
		public void start(Combatant c) {
			c.source = c.source.clone();
			c.source.walk += 10;
		}

		@Override
		public void end(Combatant c) {
			c.source.walk -= 10;
		}
	}

	/** Constructor. */
	public Longstrider() {
		super("Longstrider", 1,
				ChallengeCalculator.ratespelllikeability(1), Realm.EARTH);
		ispotion = true;
		castinbattle = true;
		castonallies = false;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		caster.addcondition(new Strider(caster, casterlevel));
		return "Walking speed for " + caster + " is now " + caster.source.walk
				+ "ft!";
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		targetself(combatant, targets);
	}
}
