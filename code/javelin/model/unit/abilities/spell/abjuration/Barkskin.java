package javelin.model.unit.abilities.spell.abjuration;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.condition.Condition;

/**
 * Gives a 1-hour bonus to
 *
 * See the d20 SRD for more info.
 */
public class Barkskin extends Touch{
	class BarkskinCondition extends Condition{
		BarkskinCondition(Spell s){
			super("barkskin",s.level,s.casterlevel,Float.MAX_VALUE,1,Effect.POSITIVE);
		}

		@Override
		public void start(Combatant c){
			c.acmodifier+=3;
		}

		@Override
		public void end(Combatant c){
			c.acmodifier-=3;
		}
	}

	/** Constructor */
	public Barkskin(){
		super("Barkskin",3,ChallengeCalculator.ratespell(2,6));
		casterlevel=6;
		castinbattle=true;
		castoutofbattle=true;
		castonallies=true;
		ispotion=true;
		isrune=new BarkskinCondition(this);
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target){
		target.addcondition(new BarkskinCondition(this));
		return target+" now has an armor class of "+target.getac()+"!";
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		return castpeacefully(caster,target);
	}
}