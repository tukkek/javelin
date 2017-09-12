package javelin.model.unit.condition.abilitydamage;

import javelin.model.unit.attack.Combatant;

public class StrengthDamage extends AbilityDamage {
	public StrengthDamage(int damage, Combatant c) {
		super(damage, c, "weak");
	}

	@Override
	void modifyability(int damage, Combatant c) {
		c.source.changestrengthscore(damage);
	}
}