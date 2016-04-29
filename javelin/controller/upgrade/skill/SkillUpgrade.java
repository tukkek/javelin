package javelin.controller.upgrade.skill;

import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * Upgrades one of the {@link Monster#skills}.
 * 
 * @author alex
 */
public abstract class SkillUpgrade extends Upgrade {
	public SkillUpgrade(String name) {
		super("Skill: " + name.toLowerCase());
	}

	@Override
	public String info(Combatant c) {
		return "Has " + getranks(c.source.skills) + " ranks in "
				+ name.toLowerCase();
	}

	/**
	 * @return the ranks of the skill in question.
	 */
	protected abstract int getranks(Skills s);

	@Override
	protected boolean apply(Combatant c) {
		int ranks = c.source.hd.count() + 3;
		if (getranks(c.source.skills) >= ranks) {
			return false;
		}
		setranks(c.source.skills, ranks);
		return true;
	}

	/**
	 * @param ranks
	 *            Sets the rank of the skill in question.
	 */
	protected abstract void setranks(Skills s, int ranks);
}