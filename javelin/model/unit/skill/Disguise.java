package javelin.model.unit.skill;

import javelin.model.unit.Combatant;
import javelin.model.unit.feat.skill.Deceitful;

public class Disguise extends Skill {
	public Disguise() {
		super("Disguise", Ability.CHARISMA);
	}

	@Override
	public int getbonus(Combatant c) {
		int bonus = super.getbonus(c);
		if (c.source.hasfeat(Deceitful.SINGLETON)) {
			bonus += getranks(c) >= 10 ? 4 : 3;
		}
		return bonus;
	}
}
