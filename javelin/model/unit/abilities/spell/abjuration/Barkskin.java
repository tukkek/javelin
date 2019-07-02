package javelin.model.unit.abilities.spell.abjuration;

import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.condition.Condition;

/**
 * Gives a 1-hour bonus to
 *
 * See the d20 SRD for more info.
 */
public class Barkskin extends Touch{
	public class BarkskinCondition extends Condition{
		public BarkskinCondition(Combatant c,Integer casterlevelp){
			super(c,"barkskin",Effect.POSITIVE,casterlevelp,Float.MAX_VALUE,1);
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
		super("Barkskin",3,ChallengeCalculator.ratespell(2,6),
				javelin.model.Realm.EARTH);
		casterlevel=6;
		castinbattle=true;
		castoutofbattle=true;
		castonallies=true;
		ispotion=true;
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target,
			List<Combatant> squad){
		target.addcondition(new BarkskinCondition(target,casterlevel));
		return target+" now has an armor class of "+target.getac()+"!";
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		return castpeacefully(caster,target,null);
	}
}