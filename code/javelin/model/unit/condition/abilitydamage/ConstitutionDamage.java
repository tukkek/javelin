package javelin.model.unit.condition.abilitydamage;

import javelin.model.unit.Combatant;

public class ConstitutionDamage extends AbilityDamage{
	public ConstitutionDamage(int damage,Combatant c){
		super(damage,c,"frail");
	}

	@Override
	void modifyability(Combatant c,int damage){
		c.source.changeconstitutionscore(c,damage);
	}
}
