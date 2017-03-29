package javelin.model.world.location.town.labor;

import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Lodge;
import javelin.model.world.location.town.Town;

public class BuildInn extends Build {
	public BuildInn() {
		super("Build " + Lodge.LEVELS[0].toLowerCase(), Lodge.LABOR[0], null,
				Town.HAMLET);
	}

	@Override
	public Location getgoal() {
		return new Lodge();
	}

	@Override
	public boolean validate(District d) {
		if (site == null && (d.getlocation(Lodge.class) != null
				|| d.isbuilding(Lodge.class))) {
			return false;
		}
		return super.validate(d);
	}
}
