package javelin.model.unit.skill;

/**
 * Opposed roll to {@link Bluff}.
 *
 * @author alex
 */
public class SenseMotive extends Skill {
	/** Constructor. */
	public SenseMotive() {
		super("Sense motive", Ability.WISDOM);
		usedincombat = true;
	}
}
