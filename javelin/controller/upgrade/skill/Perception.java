package javelin.controller.upgrade.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#perception}. This was unified by Pathfinder from the
 * original Spot and Listen d20 skills.
 * 
 * @see Stealth
 * @author alex
 */
public class Perception extends SkillUpgrade {
	public final static SkillUpgrade SINGLETON = new Perception();

	/** Constructor. */
	Perception() {
		super("Perception");
	}

	@Override
	public int getranks(Skills s) {
		return s.perception;
	}

	@Override
	public void setranks(Skills s, int ranks) {
		s.perception = ranks;
	}

	@Override
	protected int getattribute(Monster m) {
		return m.wisdom;
	}
}