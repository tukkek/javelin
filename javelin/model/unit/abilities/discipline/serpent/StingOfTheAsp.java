package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.content.action.ActionCost;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.condition.abilitydamage.StrengthDamage;
import javelin.old.RPG;

/**
 * TODO should allow combatant to be killed by secondary damage
 *
 * @author alex
 */
public class StingOfTheAsp extends Strike{
	static final int EXTRADAMAGE=RPG.average(1,6);

	public class AspString extends Condition{
		public AspString(float expireatp,Combatant c){
			super("Asp sting",null,expireatp,Effect.NEGATIVE);
			stack=true;
		}

		@Override
		public void start(Combatant c){
			// nothing at first
		}

		@Override
		public void end(Combatant c){
			c.damage(EXTRADAMAGE,0);
			c.addcondition(new StrengthDamage(2,c));
		}
	}

	public StingOfTheAsp(){
		super("Sting of the asp",2);
		ap=ActionCost.STANDARD;
	}

	@Override
	public void prehit(Combatant active,Combatant target,Attack a,BattleState s){
		a.damage[2]+=EXTRADAMAGE;
		boolean save=save(target.source.getfortitude(),12,active);
		target.addcondition(new StrengthDamage(save?1:2,target));
		if(!save) target.addcondition(new AspString(active.ap+1,target));
	}

	@Override
	public void posthit(Combatant c,Combatant target,Attack a,BattleState s){
		super.posthit(c,target,a,s);
		a.damage[2]-=EXTRADAMAGE;
	}
}
