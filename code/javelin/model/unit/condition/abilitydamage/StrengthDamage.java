package javelin.model.unit.condition.abilitydamage;

import javelin.model.unit.Combatant;

public class StrengthDamage extends AbilityDamage{
	public StrengthDamage(int damage,Combatant c){
		super(damage,c,"weak");
	}

	@Override
	void modifyability(Combatant c,int damage){
		c.source.changestrengthscore(damage);
	}
}