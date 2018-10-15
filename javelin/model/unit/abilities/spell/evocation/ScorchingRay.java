package javelin.model.unit.abilities.spell.evocation;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Ray;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * Deals 4d6 points of fire damage.
 *
 * @author alex
 */
public class ScorchingRay extends Ray{

	public ScorchingRay(){
		super("Scorching ray",2,ChallengeCalculator.ratespelllikeability(2),
				Realm.FIRE);
		castinbattle=true;
		iswand=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		target.damage(4*6/2,s,target.source.energyresistance);
		if(cn!=null) cn.overlay=new AiOverlay(target);
		return target+" is "+target.getstatus()+".";
	}
}
