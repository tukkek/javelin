package javelin.model.world.location.town.labor;

import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Inn;

public class BuildInn extends Build {
	public BuildInn() {
		super("Build " + Inn.LEVELS[0].toLowerCase(), Inn.LABOR[0], null);
	}

	@Override
	public Location getgoal() {
		return new Inn();
	}

	@Override
	public boolean validate(District d) {
		if (site == null && (d.getlocation(Inn.class) != null || d.isbuilding(Inn.class))) {
			return false;
		}
		return super.validate(d);
	}
}
