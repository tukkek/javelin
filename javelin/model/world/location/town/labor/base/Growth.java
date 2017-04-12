package javelin.model.world.location.town.labor.base;

import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Labor;

public class Growth extends Labor {
	public static final int MAXPOPULATION = 30;
	public static final Growth INSTANCE = new Growth();

	public Growth() {
		super("Growth", -1, Rank.HAMLET);
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
	}
}
