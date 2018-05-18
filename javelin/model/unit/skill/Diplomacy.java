package javelin.model.unit.skill;

import javelin.model.Realm;

public class Diplomacy extends Skill {
	static final String[] NAMES = new String[] { "Diplomacy",
			"gather information", };

	public Diplomacy() {
		super(NAMES, Ability.CHARISMA, Realm.GOOD);
		intelligent = true;
	}
}
