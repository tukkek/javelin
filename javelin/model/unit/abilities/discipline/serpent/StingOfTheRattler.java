package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.content.action.ActionCost;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.condition.Condition;
import javelin.old.RPG;

/**
 * Attack that inflicts an additional 1d4 points of damage plus 1d4 damage the
 * following round.
 *
 * TODO would be nicer to be able to kill the target on secondary damage but
 * we're not passing {@link BattleState} parameters as of yet.
 *
 * @author alex
 */
public class StingOfTheRattler extends Strike{
	static final int DAMAGEBONUS=RPG.average(1,4);

	public class RattlerSting extends Condition{
		public RattlerSting(float expireatp,Combatant c){
			super("Rattler sting",null,expireatp,Effect.NEGATIVE);
			stack=true;
		}

		@Override
		public void start(Combatant c){
			// see #hit
		}

		@Override
		public void end(Combatant c){
			c.damage(DAMAGEBONUS,0);
		}
	}

	public StingOfTheRattler(){
		super("Sting of the rattler",1);
	}

	@Override
	public void prehit(Combatant active,Combatant target,Attack a,BattleState s){
		a.damage[2]+=DAMAGEBONUS;
		target.addcondition(new RattlerSting(active.ap+ActionCost.FULL,target));
	}

	@Override
	public void posthit(Combatant c,Combatant target,Attack a,BattleState s){
		super.posthit(c,target,a,s);
		a.damage[2]-=DAMAGEBONUS;
	}
}
