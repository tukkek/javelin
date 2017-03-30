package javelin.model.world.location.town.labor;

import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;

public class Growth extends Labor {
	public static final int MAXPOPULATION = 30;

	public Growth() {
		super("Growth", -1, Town.HAMLET);
	}

	@Override
	public void done() {
		town.population += 1;
		if (town.population > 30) {
			town.population = MAXPOPULATION;
		}
	}

	@Override
	public boolean validate(District d) {
		define();
		return super.validate(d) && d.town.population < 30;
	}

	@Override
	protected void define() {
		cost = town.population;
		if (cost > 20) {
			cost += cost - 20;
		}
		if (Town.DEBUGPROJECTS) {
			name = "Growth " + town.population;
		}
	}
}
