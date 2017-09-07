package javelin.model.unit.abilities.discipline.expertise;

import javelin.controller.action.Action;
import javelin.controller.action.maneuver.Grapple;

public class GrappleManeuver extends CombatExpertiseManeuver {

	public GrappleManeuver() {
		super("Grapple");
	}

	@Override
	public Action getaction() {
		return Grapple.INSTANCE;
	}
}
