package javelin.model.unit.abilities.discipline.expertise;

import javelin.controller.content.action.Action;
import javelin.controller.content.action.maneuver.DefensiveAttack;

/** @see DefensiveAttack. */
public class DefensiveAttackManeuver extends CombatExpertiseManeuver{
	public DefensiveAttackManeuver(){
		super("Defensive attack",1);
	}

	@Override
	public Action getaction(){
		return DefensiveAttack.INSTANCE;
	}
}
