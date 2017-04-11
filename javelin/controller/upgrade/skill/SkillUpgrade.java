package javelin.controller.upgrade.skill;

import java.util.HashSet;

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
	public static final HashSet<SkillUpgrade> ALL = new HashSet<SkillUpgrade>();
	public String skillname;

	public SkillUpgrade(String name) {
		super("Skill: " + name.toLowerCase());
		skillname = name;
		ALL.add(this);
	}

	@Override
	public String inform(Combatant c) {
		return "Has " + getranks(c.source.skills) + " ranks in "
				+ name.toLowerCase();
	}

	/**
	 * @return the ranks of the skill in question.
	 */
	public abstract int getranks(Skills s);

	@Override
	protected boolean apply(Combatant c) {
		int ranks = maximize(c);
		if (getranks(c.source.skills) >= ranks) {
			return false;
		}
		setranks(c.source.skills, ranks);
		return true;
	}

	/**
	 * @return The maximum number of ranks this creature can have in any skill.
	 */
	public int maximize(Combatant c) {
		return Math.min(20, c.source.hd.count()) + 3;
	}

	/**
	 * @param ranks
	 *            Sets the rank of the skill in question.
	 */
	public abstract void setranks(Skills s, int ranks);

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SkillUpgrade
				&& ((SkillUpgrade) obj).name.equals(name);
	}

	/**
	 * @return The relevant attribute score for this skill.
	 */
	abstract protected int getattribute(Monster m);

	/**
	 * Like {@link #getranks(Skills)} except...
	 * 
	 * @return The total of ranks with the ability modifier.
	 */
	public int gettotal(Monster m) {
		return getranks(m.skills) + Monster.getbonus(getattribute(m));
	}
}