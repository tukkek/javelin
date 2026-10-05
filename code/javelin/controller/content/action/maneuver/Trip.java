package javelin.controller.content.action.maneuver;

import javelin.Javelin;
import javelin.controller.ai.ChanceNode;
import javelin.controller.content.action.Action;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.condition.Prone;
import javelin.model.unit.feat.attack.expertise.ImprovedTrip;

public class Trip extends ExpertiseAction{

	public static final Action INSTANCE=new Trip();

	private Trip(){
		super("Trip","T",ImprovedTrip.SINGLETON,+2);
	}

	@Override
	boolean validatetarget(Combatant target){
		return target.hascondition(Prone.class)==null;
	}

	@Override
	ChanceNode miss(Combatant combatant,Combatant target,BattleState battleState,
			float chance){
		return new ChanceNode(battleState,chance,"Trip attemp fails...",
				Javelin.Delay.WAIT);
	}

	@Override
	ChanceNode hit(Combatant current,Combatant target,BattleState s,float chance){
		s=s.clone();
		current=s.clone(current);
		target=s.clone(target);
		target.addcondition(new Prone(target.ap+.1f,target));
		return new ChanceNode(s,chance,target+" is prone!",Javelin.Delay.BLOCK);
	}

	@Override
	int getsavebonus(Combatant targetCombatant){
		return targetCombatant.source.ref;
	}

	@Override
	int getattackerbonus(Combatant combatant){
		return Monster.getbonus(combatant.source.dexterity);
	}

}
