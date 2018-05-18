package javelin.model.unit.skill;

import javelin.model.Realm;

public class UseMagicDevice extends Skill {
	public UseMagicDevice() {
		super("Use magic device", Ability.CHARISMA, Realm.MAGIC);
		usedincombat = true;
		intelligent = true;
	}
}
