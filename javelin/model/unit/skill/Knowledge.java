package javelin.model.unit.skill;

import javelin.model.Realm;

public class Knowledge extends Skill {
	public Knowledge() {
		super("Knowledge", Ability.INTELLIGENCE, Realm.WATER);
		intelligent = true;
	}
}
