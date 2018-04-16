package javelin.model.unit.abilities.spell.transmutation;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;

/**
 * See the d20 SRD for more info.
 */
public class Fly extends Touch {
	public class Flying extends Condition {
		int original;

		/**
		 * Constructor.
		 * 
		 * @param casterlevelp
		 */
		public Flying(Combatant c, Integer casterlevelp) {
			super(Float.MAX_VALUE, c, Effect.POSITIVE, "flying", casterlevelp);
		}

		@Override
		public void start(Combatant c) {
			original = c.source.fly;
			c.source.fly = 60;
		}

		@Override
		public void end(Combatant c) {
			c.source.fly = Math.min(c.source.fly, original);
		}
	}

	/** Constructor. */
	public Fly() {
		super("Fly", 3, ChallengeCalculator.ratespelllikeability(3),
				Realm.AIR);
		castinbattle = true;
		castonallies = true;
		ispotion = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		target.addcondition(new Flying(target, casterlevel));
		return target + " floats above the ground!";
	}
}
