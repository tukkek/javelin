package javelin.model.unit.abilities.spell.illusion;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.condition.Condition;

public class Displacement extends Touch{
	public class Blinking extends Condition{
		public Blinking(float expireatp,Combatant c,Integer casterlevelp){
			super(expireatp,c,Effect.POSITIVE,"blinking",casterlevelp);
		}

		@Override
		public void start(Combatant c){
			c.source=c.source.clone();
			c.source.misschance+=.5;
		}

		@Override
		public void end(Combatant c){
			c.source=c.source.clone();
			c.source.misschance-=.5;
		}
	}

	protected int turns=6;

	public Displacement(){
		this("Displacement",3,ChallengeCalculator.ratespelllikeability(3),
				Realm.MAGIC);
		iswand=true;
	}

	public Displacement(String name,int levelp,float incrementcost,Realm realmp){
		super(name,levelp,incrementcost,realmp);
		castinbattle=true;
		castonallies=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		target.addcondition(new Blinking(caster.ap+turns,caster,casterlevel));
		return target+" is blinking!";
	}
}
