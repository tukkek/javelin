package javelin.model.world.location.town.labor;

import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Inn;

public class BuildInn extends Build {
	public BuildInn() {
		super("Build inn", 5, null);
	}

	@Override
	public Inn getgoal() {
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
