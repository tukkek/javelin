package javelin.model.unit.condition.abilitydamage;

import javelin.model.unit.attack.Combatant;

public class WisdomDamage extends AbilityDamage {
	public WisdomDamage(int damage, Combatant c) {
		super(damage, c, "derpy");
	}

	@Override
	void modifyability(int damage, Combatant c) {
		c.source.changewisdomscore(damage);
	}
}