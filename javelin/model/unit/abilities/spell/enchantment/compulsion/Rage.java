package javelin.model.unit.abilities.spell.enchantment.compulsion;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Condition;

public class Rage extends Spell{
	public class Raging extends Condition{
		public Raging(float expireatp,Combatant c,Integer casterlevel){
			super(c,"raging",Effect.POSITIVE,casterlevel,expireatp);
		}

		@Override
		public void start(Combatant c){
			Monster m=c.source.clone();
			c.source=m;
			m.changestrengthmodifier(+1);
			m.changeconstitutionmodifier(c,+1);
			m.addwill(+1);
			c.acmodifier-=2;
		}

		@Override
		public void end(Combatant c){
			Monster m=c.source.clone();
			c.source=m;
			m.changestrengthmodifier(-1);
			m.changeconstitutionmodifier(c,-1);
			m.addwill(-1);
			c.acmodifier+=2;
		}
	}

	public Rage(){
		this("Rage",3,ChallengeCalculator.ratespelllikeability(3));
	}

	public Rage(String name,int levelp,float incrementcost){
		super(name,levelp,incrementcost);
		castinbattle=true;
		castonallies=true;
		ispotion=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		float expiresat=caster.ap+getduration(target);
		target.addcondition(new Raging(expiresat,target,casterlevel));
		return target+" is raging!";
	}

	float getduration(Combatant target){
		return Math.max(1,4+Monster.getbonus(target.source.constitution));
	}
}
