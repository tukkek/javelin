package javelin.model.unit.abilities.spell.enchantment.compulsion;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.condition.Heroic;

/**
 * See the d20 SRD for more info.
 */
public class Heroism extends Touch{
	/** Constructor. */
	public Heroism(){
		super("Heroism",3,ChallengeCalculator.ratespell(3));
		castonallies=true;
		castinbattle=true;
		ispotion=true;
		isrune=new Heroic(this);
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		target.addcondition(new Heroic(this));
		return target+" is heroic!";
	}

}
