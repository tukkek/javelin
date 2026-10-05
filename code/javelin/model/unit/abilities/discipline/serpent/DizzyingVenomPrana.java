package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.content.action.ActionCost;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.condition.abilitydamage.WisdomDamage;

public class DizzyingVenomPrana extends Strike{
	public DizzyingVenomPrana(){
		super("Dizzying venom prana",1);
	}

	@Override
	public void prehit(Combatant active,Combatant target,Attack a,BattleState s){
		target.ap+=ActionCost.PARTIAL;
		if(save(target.source.getfortitude(),11,active))
			target.addcondition(new WisdomDamage(2,target));
	}
}
