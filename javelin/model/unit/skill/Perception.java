package javelin.model.unit.skill;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.feat.skill.Alertness;

/**
 * A mix of Search, Spot and Listen. Only use directly for basic perception
 * rolls that don't depent on {@link Weather}, {@link Javelin#getDayPeriod()},
 * flying or similar modifiers.
 *
 * @see Squad#perceive(boolean, boolean, boolean)
 * @see Combatant#perceive(boolean, boolean, boolean)
 */
public class Perception extends Skill {
	static final String[] NAMES = new String[] { "Perception", "listen", "spot",
			"search" };

	public Perception() {
		super(NAMES, Ability.WISDOM);
	}

	@Override
	public int getbonus(Combatant c) {
		int bonus = super.getbonus(c);
		if (c.source.hasfeat(Alertness.SINGLETON)) {
			bonus += Alertness.BONUS;
		}
		return bonus;
	}
}
