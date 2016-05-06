package javelin.model.world.place.town.research;

import javelin.model.world.place.town.Accommodations;
import javelin.model.world.place.town.Town;
import javelin.view.screen.town.ResearchScreen;

/**
 * Allows a Town to upgrade it's {@link Town#lodging}.
 * 
 * @author alex
 */
public class AccommodationResearch extends Research {
	final Accommodations tier;

	/**
	 * @param tierp
	 *            {@link Accommodations} tier to advance to upon research
	 *            completion.
	 */
	public AccommodationResearch(Accommodations tierp) {
		super("Accommodation: " + tierp.toString(), tierp.getprice());
		tier = tierp;
	}

	@Override
	public void apply(Town t, ResearchScreen s) {
		t.lodging = tier;
	}

	@Override
	public boolean isrepeated(Town t) {
		return false;
	}
}
