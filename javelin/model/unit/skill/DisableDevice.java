package javelin.model.unit.skill;

import javelin.model.Realm;

public class DisableDevice extends Skill {
	public DisableDevice() {
		super("Disable device", Ability.DEXTERITY, Realm.AIR);
		intelligent = true;
	}
}
