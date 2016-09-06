package javelin.controller.upgrade.skill;

import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * Upgrades {@link Skills#disguise}.
 * 
 * @author alex
 */
@SuppressWarnings("deprecation")
public class Disguise extends SkillUpgrade {
	/** Unique instance for this {@link Upgrade}. */
	public static final Upgrade SINGLETON = new Disguise();

	private Disguise() {
		super("Disguise");
	}

	@Override
	public int getranks(Skills s) {
		return s.disguise;
	}

	@Override
	public void setranks(Skills s, int ranks) {
		s.disguise = ranks;
	}

	@Override
	protected int getattribute(Monster m) {
		return m.charisma;
	}
}
