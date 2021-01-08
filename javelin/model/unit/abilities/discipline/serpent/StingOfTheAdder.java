package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.content.action.ActionCost;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.condition.abilitydamage.WisdomDamage;
import javelin.old.RPG;

public class StingOfTheAdder extends Strike{
	static final int DAMAGE=RPG.average(5,6);
	static final int WISDOMDAMAGE=RPG.average(1,4);
	static final int SECONDARYDAMAGE=RPG.average(1,6);
	static final int SECONDARYWISDOMDAMAGE=2;

	class AdderSting extends Condition{
		public AdderSting(float expireatp,Combatant c){
			super("adder sting",null,expireatp,Effect.NEGATIVE);
		}

		@Override
		public void start(Combatant c){
			// wait to expire
		}

		@Override
		public void end(Combatant c){
			c.damage(SECONDARYDAMAGE,0);
			c.addcondition(new WisdomDamage(SECONDARYWISDOMDAMAGE,c));
		}
	}

	public StingOfTheAdder(){
		super("Sting of the adder",4);
		ap=ActionCost.STANDARD;
	}

	@Override
	public void prehit(Combatant current,Combatant target,Attack a,BattleState s){
		target.damage(DAMAGE,s,0);
		final boolean save=save(target.source.getfortitude(),14,current);
		final int wisdomdamage=save?WISDOMDAMAGE/2:WISDOMDAMAGE;
		target.addcondition(new WisdomDamage(wisdomdamage,target));
		if(!save) target.addcondition(new AdderSting(current.ap+1,target));
	}
}
