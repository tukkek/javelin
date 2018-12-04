package javelin.controller.event.wild.positive;

import javelin.Javelin;
import javelin.controller.event.wild.Wanderer;
import javelin.model.unit.Squad;
import javelin.model.world.location.PointOfInterest;

/**
 * Event: recovers all {@link Squad} members.
 *
 * @author alex
 */
public class WanderingPriest extends Wanderer{
	static final String MESSAGE="You come accross a wandering priest, who heals all your wounds before continuing his journey!";

	public WanderingPriest(){
		super("Wandering priest");
	}

	@Override
	public void happen(Squad s,PointOfInterest l){
		for(var member:Squad.active.members)
			member.heal(member.maxhp,true);
		Javelin.message(MESSAGE,true);
	}
}
