package javelin.controller.event.wild.neutral;

import javelin.Javelin;
import javelin.controller.event.wild.WildEvent;
import javelin.model.unit.Squad;
import javelin.model.world.Caravan;
import javelin.model.world.location.PointOfInterest;

/**
 * Spawns a {@link Caravan}.
 *
 * @author alex
 */
public class FindCaravan extends WildEvent{
	/** Reflection-friendly constructor. */
	public FindCaravan(PointOfInterest l){
		super("Find caravan",l);
	}

	@Override
	public void happen(Squad s){
		new Caravan(location.x,location.y,s.getel()).place();
		Javelin.redraw();
		Javelin.message("You come across a caravan of merchants!",true);
	}
}
