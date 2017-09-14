package javelin.model.unit.abilities.spell.abjuration;

import javelin.controller.challenge.CrCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;

/**
 * Gives a 1-hour bonus to
 * 
 * See the d20 SRD for more info.
 */
public class Barkskin extends Touch {
	public class BarkskinCondition extends Condition {
		public BarkskinCondition(Combatant c, Integer casterlevelp) {
			super(Float.MAX_VALUE, c, Effect.POSITIVE, "barkskin", casterlevelp,
					1);
		}

		@Override
		public void start(Combatant c) {
			c.acmodifier += 3;
		}

		@Override
		public void end(Combatant c) {
			c.acmodifier -= 3;
		}
	}

	/** Constructor */
	public Barkskin() {
		super("Barkskin", 3,
				CrCalculator.ratespelllikeability(2, 6),
				javelin.model.Realm.EARTH);
		casterlevel = 6;
		castinbattle = true;
		castoutofbattle = true;
		castonallies = true;
		ispotion = true;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		target.addcondition(new BarkskinCondition(target, casterlevel));
		return target + " now has an armor class of " + target.ac() + "!";
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		return castpeacefully(caster, target);
	}
}