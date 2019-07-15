package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.action.ActionCost;
import javelin.controller.action.ai.attack.DamageChance;
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
			super(c,"Rattler sting",Effect.NEGATIVE,null,expireatp);
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
	public void hit(Combatant active,Combatant target,Attack a,DamageChance dc,
			BattleState s){
		dc.damage+=DAMAGEBONUS;
		target.addcondition(new RattlerSting(active.ap+ActionCost.FULL,target));
	}
}
