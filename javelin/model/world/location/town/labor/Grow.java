package javelin.model.world.location.town.labor;

import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;

public class Grow extends Labor {

	public Grow(Town t) {
		super("Grow", t.population, t);
	}

	@Override
	public void done() {
		town.population += 1;
	}

	@Override
	public boolean validate(District d) {
		cost = town.population;
		return true;
	}
}
