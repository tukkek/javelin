package javelin.model.unit.condition.abilitydamage;

import javelin.model.unit.attack.Combatant;

public class WisdomDamage extends AbilityDamage {
	public WisdomDamage(int damage, Combatant c) {
		super(damage, c, "derpy");
	}

	@Override
	void modifyability(Combatant c, int damage) {
		c.source.changewisdomscore(damage);
	}
}