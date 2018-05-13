package javelin.model.unit.skill;

public class UseMagicDevice extends Skill {
	public UseMagicDevice() {
		super("Use magic device", Ability.CHARISMA);
		usedincombat = true;
		intelligent = true;
	}
}
