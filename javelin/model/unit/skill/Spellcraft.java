package javelin.model.unit.skill;

public class Spellcraft extends Skill {
	public Spellcraft() {
		super(new String[] { "Spellcraft", "Scry" }, Ability.INTELLIGENCE);
		intelligent = true;
	}
}
