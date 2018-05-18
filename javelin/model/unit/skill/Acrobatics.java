package javelin.model.unit.skill;

import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.feat.skill.Acrobatic;

public class Acrobatics extends Skill {
	static final String[] NAMES = new String[] { "Acrobatics", "tumble",
			"balance", "escape artist" };

	public Acrobatics() {
		super(NAMES, Ability.DEXTERITY, Realm.AIR);
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
