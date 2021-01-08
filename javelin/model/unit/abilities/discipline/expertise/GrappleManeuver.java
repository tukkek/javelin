package javelin.model.unit.abilities.discipline.expertise;

import javelin.controller.content.action.Action;
import javelin.controller.content.action.maneuver.Grapple;

public class GrappleManeuver extends CombatExpertiseManeuver{

	public GrappleManeuver(){
		super("Grapple",2);
	}

	@Override
	public Action getaction(){
		return Grapple.INSTANCE;
	}
}
