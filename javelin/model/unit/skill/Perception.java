package javelin.model.unit.skill;

import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.feat.skill.Alertness;

/**
 * @see Squad#perceive(boolean, boolean, boolean)
 * @see Combatant#perceive(boolean, boolean, boolean)
 */
public class Perception extends Skill {
	public Perception() {
		super("Perception", Ability.WISDOM);
	}

	@Override
	public int getbonus(Combatant c) {
		int bonus = super.getbonus(c);
		if (c.source.hasfeat(Alertness.SINGLETON)) {
			/** +1 since we don't support Sense Motive in the game. */
			bonus += getranks(c) >= 10 ? +3 : +5;
		}
		return bonus;
	}
}
