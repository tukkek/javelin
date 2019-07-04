package javelin.model.unit.abilities.spell;

import javelin.controller.action.ai.attack.RangedAttack;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * A ranged touch attack spell.
 *
 * @author alex
 */
public abstract class Ray extends Spell{

	public Ray(String name,int level,float incrementcost){
		super(name,level,incrementcost);
	}

	@Override
	public int hit(Combatant active,Combatant target,BattleState state){
		if(automatichit) return Integer.MIN_VALUE;
		int bonus=active.source.getbab()+Monster.getbonus(active.source.dexterity);
		int ac;
		if(castonallies){
			if(Walker.distance(active,target)<2) return Integer.MIN_VALUE;
			ac=10;
		}else{
			ac=target.gettouchac();
			bonus-=RangedAttack.SINGLETON.getpenalty(active,target,state);
		}
		return ac-bonus;
	}
}