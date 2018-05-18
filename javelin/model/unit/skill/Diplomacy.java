package javelin.model.unit.skill;

public class Diplomacy extends Skill {
	public Diplomacy() {
		super(new String[] { "Diplomacy", "gather information", },
				Ability.CHARISMA);
		intelligent = true;
	}
}
