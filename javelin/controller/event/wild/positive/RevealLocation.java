package javelin.controller.event.wild.positive;

import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.event.wild.WildEvent;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.PointOfInterest;
import javelin.view.screen.WorldScreen;

/**
 * Shows an unrevealed nearby location.
 *
 * @author alex
 */
public class RevealLocation extends WildEvent{
	List<Actor> undiscovered;

	/** Constructor. */
	public RevealLocation(){
		super("Reveal location");
	}

	@Override
	public void happen(Squad s,PointOfInterest l){
		var here=l.getlocation();
		undiscovered.sort((a,b)->Double.compare(a.getlocation().distance(here),
				b.getlocation().distance(here)));
		var show=undiscovered.get(0);
		WorldScreen.discover(show.x,show.y);
		Javelin.redraw();
		var message="A traveller tells you the location of: "+show.describe();
		Javelin.message(message,true);
	}

	@Override
	public boolean validate(Squad s,int squadel,PointOfInterest l){
		undiscovered=World.getactors().stream()
				.filter(a->a instanceof Location&&!WorldScreen.see(a.getlocation()))
				.collect(Collectors.toList());
		return !undiscovered.isEmpty();
	}
}
