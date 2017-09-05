package javelin.model.unit.discipline.expertise;

import javelin.controller.action.Action;
import javelin.controller.action.maneuver.DefensiveAttack;

/** @see DefensiveAttack. */
public class DefensiveAttackManeuver extends CombatExpertiseManeuver {
	public DefensiveAttackManeuver() {
		super("Defensive attack");
	}

	@Override
	public Action getaction() {
		return DefensiveAttack.INSTANCE;
	}
}
