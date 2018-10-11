package javelin.model.unit.abilities.spell.enchantment.compulsion;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.condition.Heroic;

/**
 * See the d20 SRD for more info.
 */
public class Heroism extends Touch{
	public Heroism(){
		super("Heroism",3,ChallengeCalculator.ratespelllikeability(3),Realm.FIRE);
		castonallies=true;
		castinbattle=true;
		ispotion=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		target.addcondition(new Heroic(target,casterlevel));
		return target+" is heroic!";
	}

}
