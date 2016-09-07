package javelin.controller.action.world.improvement;

import javelin.model.world.Improvement;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;

public class BuildOutpost extends Improvement {
	public BuildOutpost(String name, double price, Character key,
			boolean absolute) {
		super(name, price, key, absolute, true);
	}

	@Override
	public Location done(int x, int y) {
		Outpost o = new Outpost();
		o.setlocation(x, y);
		return o;
	}
}
