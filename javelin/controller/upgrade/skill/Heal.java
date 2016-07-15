package javelin.controller.upgrade.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.Skills;
import javelin.model.world.location.town.Town;

/**
 * While resting, allows a player to cure poison and damage on others.
 * 
 * @see Town#rest(int, long)
 * @author alex
 */
public class Heal extends SkillUpgrade {
	public final static SkillUpgrade SINGLETON = new Heal();

	/** Constructor. */
	Heal() {
		super("Heal");
	}

	@Override
	public int getranks(Skills s) {
		return s.heal;
	}

	@Override
	public void setranks(Skills s, int ranks) {
		s.heal = ranks;
	}

	@Override
	protected int getattribute(Monster m) {
		return m.wisdom;
	}
}
