package javelin.controller.upgrade.skill;

import javelin.model.unit.Skills;
import javelin.model.world.location.town.Town;

/**
 * While resting, allows a player to cure poison and damage on others.
 * 
 * @see Town#rest(int, long)
 * @author alex
 */
public class Heal extends SkillUpgrade {
	/** Constructor. */
	public Heal() {
		super("heal");
	}

	@Override
	protected int getranks(Skills s) {
		return s.heal;
	}

	@Override
	protected void setranks(Skills s, int ranks) {
		s.heal = ranks;
	}
}
