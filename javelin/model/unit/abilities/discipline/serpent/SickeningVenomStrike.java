package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.action.ai.attack.DamageChance;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.condition.Sickened;
import javelin.model.unit.condition.abilitydamage.ConstitutionDamage;
import javelin.old.RPG;

public class SickeningVenomStrike extends Strike{
	static final int SICKENDURATION=RPG.average(1,4);

	public SickeningVenomStrike(){
		super("Sickening Venom Strike",3);
	}

	@Override
	public void hit(Combatant current,Combatant target,Attack a,DamageChance dc,
			BattleState s){
		target.addcondition(new ConstitutionDamage(2,current));
		if(!save(target.source.getfortitude(),13,current)){
			final float expireat=current.ap+SICKENDURATION;
			target.addcondition(new Sickened(expireat,target,null));
		}
	}
}
