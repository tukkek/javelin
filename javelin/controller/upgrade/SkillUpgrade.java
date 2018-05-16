package javelin.controller.upgrade;

import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.skill.Skill;

/**
 * Makes a {@link Skill} trained.
 *
 * @see Monster#trained
 * @see ClassLevelUpgrade
 * @author alex
 */
public class SkillUpgrade extends Upgrade {
	Skill skill;

	public SkillUpgrade(Skill s) {
		super("Skill: " + s.name.toLowerCase());
		skill = s;
		usedincombat = s.usedincombat;
	}

	@Override
	public String inform(Combatant c) {
		return "Currently: " + skill.name + " " + skill.getsignedbonus(c)
				+ " (untrained)";
	}

	@Override
	protected boolean apply(Combatant c) {
		if (c.source.trained.contains(skill.name)) {
			return false;
		}
		Monster m = c.source;
		if (!skill.canuse(c)) {
			return false;
		}
		m.trained.add(skill.name);
		skill.maximize(m);
		return true;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SkillUpgrade
				&& ((SkillUpgrade) obj).name.equals(name);
	}
}