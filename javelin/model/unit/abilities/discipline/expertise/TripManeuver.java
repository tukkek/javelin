package javelin.model.unit.abilities.discipline.expertise;

import javelin.controller.action.Action;
import javelin.controller.action.maneuver.Trip;

public class TripManeuver extends CombatExpertiseManeuver{
	public TripManeuver(){
		super("Trip",2);
	}

	@Override
	public Action getaction(){
		return Trip.INSTANCE;
	}
}
