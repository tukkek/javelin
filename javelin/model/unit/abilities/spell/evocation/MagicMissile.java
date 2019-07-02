package javelin.model.unit.abilities.spell.evocation;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * Deals 1d4+1 points of force damage.
 *
 * @author alex
 */
public class MagicMissile extends Spell{
	/** Constructor. */
	public MagicMissile(){
		super("Magic missile",1,ChallengeCalculator.ratespelllikeability(1));
		castinbattle=true;
		iswand=true;
		continuous=1;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		target.damage(1*4/2+1,s,0);
		if(cn!=null) cn.overlay=new AiOverlay(target);
		return target+" is "+target.getstatus()+"!";
	}

	@Override
	public int save(Combatant caster,Combatant target){
		return Integer.MIN_VALUE;
	}
}
