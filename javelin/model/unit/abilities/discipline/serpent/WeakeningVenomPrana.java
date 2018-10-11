package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.action.ai.attack.DamageChance;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.condition.abilitydamage.StrengthDamage;
import javelin.old.RPG;

public class WeakeningVenomPrana extends Strike{
	static final int DAMAGE=RPG.average(1,4);

	public WeakeningVenomPrana(){
		super("Weakening venom prana",2);
	}

	@Override
	public void preattacks(Combatant current,Combatant target,Attack a,
			BattleState s){
		// nothing
	}

	@Override
	public void postattacks(Combatant current,Combatant target,Attack a,
			BattleState s){
		// nothing
	}

	@Override
	public void prehit(Combatant current,Combatant target,Attack a,
			DamageChance dc,BattleState s){
		final int damage=save(target.source.getfortitude(),12,current)?DAMAGE/2
				:DAMAGE;
		target.addcondition(new StrengthDamage(damage,target));
	}

	@Override
	public void posthit(Combatant current,Combatant target,Attack a,
			DamageChance dc,BattleState s){
		// nothing
	}
}
