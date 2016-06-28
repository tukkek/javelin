package javelin.controller.terrain.hazard;

import javelin.Javelin;
import javelin.model.unit.Squad;
import javelin.model.world.Season;

/**
 * Ship gets stuck on ice for 24h.
 * 
 * @author alex
 */
public class Ice extends Hazard {

	@Override
	public void hazard(int hoursellapsed) {
		Squad.active.hourselapsed += 24;
		Javelin.message("Your ship gets stuck on ice!", true);
	}

	@Override
	public boolean validate() {
		return Season.current == Season.WINTER && Squad.active.transport != null
				&& Squad.active.transport.sails
				&& !Squad.active.transport.flies;
	}

}
