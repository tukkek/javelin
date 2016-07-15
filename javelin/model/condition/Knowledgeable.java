package javelin.model.condition;

import javelin.controller.upgrade.skill.SkillUpgrade;
import javelin.model.item.relic.Crown;
import javelin.model.unit.Combatant;

/**
 * @see Crown
 * @author alex
 */
public class Knowledgeable extends Condition {
	/** Constructor. */
	public Knowledgeable(Combatant c) {
		super(Float.MAX_VALUE, c, Effect.NEUTRAL, "all-knowing", 20, 24 * 7);
	}

	@Override
	public void start(Combatant c) {
		for (SkillUpgrade s : SkillUpgrade.ALL) {
			s.setranks(c.source.skills, s.getranks(c.source.skills) + 5);
		}
	}

	@Override
	public void end(Combatant c) {
		for (SkillUpgrade s : SkillUpgrade.ALL) {
			s.setranks(c.source.skills, s.getranks(c.source.skills) - 5);
		}
	}
}
