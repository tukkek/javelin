package javelin.model.world.location.town.labor.basic;

import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Labor;

public class Redraw extends Labor {
	public Redraw() {
		super("Redraw", 0, Rank.HAMLET);
		automatic = false;
	}

	@Override
	protected void define() {
		cost = Math.min(town.population, town.getgovernor().gethandsize());
	}

	@Override
	public void done() {
		for (Labor l : town.getgovernor().gethand()) {
			l.discard();
		}
		town.getgovernor().redraw();
	}
}
