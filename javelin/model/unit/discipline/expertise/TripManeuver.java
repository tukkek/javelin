package javelin.model.unit.discipline.expertise;

import javelin.controller.action.Action;
import javelin.controller.action.maneuver.Trip;
import javelin.model.unit.discipline.expertise.CombatExpertiseManeuver;

public class TripManeuver extends CombatExpertiseManeuver {
	public TripManeuver() {
		super("Trip");
	}

	@Override
	public Action getaction() {
		return Trip.INSTANCE;
	}
}
