package javelin.model.unit.abilities.spell.abjuration;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.condition.Condition;

/**
 * See the d20 SRD for more info.
 *
 * @see Monster#energyresistance
 */
public class ResistEnergy extends Touch{
	class Resistant extends Condition{
		int r;

		/**
		 * @param resistance Number of {@link Monster#energyresistance} points.
		 * @param casterlevelp
		 */
		Resistant(int resistance,Spell s){
			super("resistant",s,Float.MAX_VALUE,1,Effect.POSITIVE);
			r=resistance;
		}

		@Override
		public void start(Combatant c){
			c.source.energyresistance+=r;
		}

		@Override
		public void end(Combatant c){
			c.source.energyresistance-=r;
		}
	}

	int resistance;

	/** Constructor. */
	public ResistEnergy(){
		super("Resist energy",2,ChallengeCalculator.ratespell(2,7));
		resistance=20/5;
		casterlevel=7;
		castinbattle=true;
		castonallies=true;
		castoutofbattle=true;
		ispotion=true;
		isrune=new Resistant(resistance,this);
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target){
		target.addcondition(new Resistant(resistance,this));
		return target+" is looking reflective!";
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		return castpeacefully(caster,target);
	}
}