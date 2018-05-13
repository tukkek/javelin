package javelin.model.unit.skill;

import javelin.model.unit.Combatant;
import javelin.model.unit.feat.CombatCasting;

public class Concentration extends Skill {
	public Concentration() {
		super("Concentration", Ability.CONSTITUTION);
	}

	@Override
	public int getbonus(Combatant c) {
		int bonus = super.getbonus(c);
		if (c.source.hasfeat(CombatCasting.SINGLETON)) {
			bonus += 4;
		}
		return bonus;
	}
}
