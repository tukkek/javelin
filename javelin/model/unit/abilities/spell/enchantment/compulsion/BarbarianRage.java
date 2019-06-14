package javelin.model.unit.abilities.spell.enchantment.compulsion;

import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class BarbarianRage extends Rage{
	public BarbarianRage(){
		super("Barbarian rage",1,ChallengeCalculator.ratespelllikeability(1));
		ispotion=false;
		provokeaoo=false;
	}

	@Override
	float getduration(Combatant target){
		return super.getduration(target)/2f;
	}

	@Override
	public void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		targetself(combatant,targets);
	}
}
