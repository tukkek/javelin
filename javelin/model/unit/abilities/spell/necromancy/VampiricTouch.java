package javelin.model.unit.abilities.spell.necromancy;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.condition.Condition;

/**
 * See the d20 SRD for more info.
 */
public class VampiricTouch extends Touch{
	public class Vampiric extends Condition{
		int steal;

		public Vampiric(float expireat,Combatant caster,int steal,
				Integer casterlevelp){
			super(caster,"vampiric",Effect.POSITIVE,casterlevelp,expireat,1);
			this.steal=steal;
		}

		@Override
		public void start(Combatant c){}

		@Override
		public void end(Combatant c){
			c.damage(steal);
		}

		@Override
		public void finish(BattleState s){}

		@Override
		public void merge(Combatant c,Condition condition){
			steal+=((Vampiric)condition).steal;
		}
	}

	/** Constructor. */
	public VampiricTouch(){
		super("Vampiric touch",3,ChallengeCalculator.ratespelllikeability(3),
				Realm.EVIL);
		castinbattle=true;
		provokeaoo=false;
	}

	@Override
	public String cast(final Combatant caster,final Combatant target,
			final boolean saved,final BattleState s,ChanceNode cn){
		int steal=21;
		final int max=target.hp+10;
		if(steal>max) steal=max;
		target.damage(steal,s,target.source.energyresistance);
		final int originalhp=caster.hp;
		caster.heal(steal,true);
		caster.addcondition(
				new Vampiric(Float.MAX_VALUE,caster,caster.hp-originalhp,casterlevel));
		return describe(target)+"\n"+describe(caster);
	}

	public String describe(final Combatant c){
		return c+" is "+c.getstatus()+".";
	}

}
