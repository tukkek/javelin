package javelin.model.unit.abilities.spell.transmutation;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.condition.Condition;

/**
 * See the d20 SRD for more info.
 */
public class Fly extends Touch{
	class Flying extends Condition{
		int original;

		/**
		 * Constructor.
		 *
		 * @param casterlevelp
		 */
		public Flying(Spell s){
			super("flying",s,Float.MAX_VALUE,Effect.POSITIVE);
		}

		@Override
		public void start(Combatant c){
			original=c.source.fly;
			c.source.fly=60;
		}

		@Override
		public void end(Combatant c){
			c.source.fly=Math.min(c.source.fly,original);
		}
	}

	/** Constructor. */
	public Fly(){
		super("Fly",3,ChallengeCalculator.ratespell(3));
		castinbattle=true;
		castonallies=true;
		ispotion=true;
		isrune=new Flying(this);
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		target.addcondition(new Flying(this));
		return target+" floats above the ground!";
	}
}
