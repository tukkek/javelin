package javelin.model.unit.skill;

import javelin.model.Realm;

/**
 * Opposed roll to {@link Bluff}.
 *
 * @author alex
 */
public class SenseMotive extends Skill{
	/** Constructor. */
	public SenseMotive(){
		super("Sense motive",Ability.WISDOM,Realm.EARTH);
		usedincombat=true;
	}
}
