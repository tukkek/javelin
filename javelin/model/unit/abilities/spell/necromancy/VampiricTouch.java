package javelin.model.unit.abilities.spell.necromancy;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.condition.Condition;

/**
 * See the d20 SRD for more info.
 */
public class VampiricTouch extends Touch{
	class Vampiric extends Condition{
		int steal;

		Vampiric(float expireat,Combatant caster,int steal,Integer casterlevelp){
			super(caster,"vampiric",Effect.POSITIVE,casterlevelp,expireat,1);
			this.steal=steal;
		}

		@Override
		public void start(Combatant c){
		}

		@Override
		public void end(Combatant c){
			c.damage(steal,c.source.energyresistance);
		}

		@Override
		public void finish(BattleState s){
		}

		@Override
		public void merge(Combatant c,Condition condition){
			steal+=((Vampiric)condition).steal;
		}
	}

	/** Constructor. */
	public VampiricTouch(){
		super("Vampiric touch",3,ChallengeCalculator.ratespell(3));
		castinbattle=true;
		provokeaoo=false;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		var steal=21;
		var max=target.hp+10;
		if(steal>max) steal=max;
		target.damage(steal,s,target.source.energyresistance);
		var originalhp=caster.hp;
		caster.heal(steal,true);
		var c=new Vampiric(Float.MAX_VALUE,caster,caster.hp-originalhp,casterlevel);
		caster.addcondition(c);
		return describe(target)+"\n"+describe(caster);
	}

	String describe(Combatant c){
		return c+" is "+c.getstatus()+".";
	}
}
