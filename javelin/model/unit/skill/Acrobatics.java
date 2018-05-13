package javelin.model.unit.skill;

import javelin.model.unit.Combatant;
import javelin.model.unit.feat.skill.Acrobatic;

public class Acrobatics extends Skill {
	public Acrobatics() {
		super(new String[] { "Acrobatics", "tumble" }, Ability.DEXTERITY);
		usedincombat = true;
	}

	@Override
	public int getbonus(Combatant c) {
		int bonus = super.getbonus(c);
		if (c.source.hasfeat(Acrobatic.SINGLETON)) {
			bonus += +Acrobatic.BONUS;
		}
		return bonus;
	}
}
