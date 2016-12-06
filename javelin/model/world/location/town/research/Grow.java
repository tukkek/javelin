package javelin.model.world.location.town.research;

import javelin.model.world.location.town.Town;

public class Grow extends Labor {

	public Grow(Town t) {
		super("Grow", "Makes this town produce more labor and possibly grow in size", t.size, t);
	}

	@Override
	public void done() {
		town.size += 1;
	}

	@Override
	public boolean validate() {
		cost = town.size;
		return true;
	}
}
