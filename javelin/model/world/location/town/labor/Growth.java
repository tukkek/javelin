package javelin.model.world.location.town.labor;

import javelin.model.world.location.town.District;

public class Growth extends Labor {

	public Growth() {
		super("Growth");
	}

	@Override
	public void done() {
		town.population += 1;
	}

	@Override
	public boolean validate(District d) {
		define();
		return true;
	}

	@Override
	protected void define() {
		cost = town.population;
	}
}
