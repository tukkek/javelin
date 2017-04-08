package javelin.model.world.location.town.labor;

import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.unique.UniqueLocation;

public class BuildUnique extends Build {
	private UniqueLocation goal;

	public BuildUnique(int cost, UniqueLocation goal, Rank minimumrank) {
		super("Build " + goal.toString().toLowerCase(), cost, null,
				minimumrank);
		UniqueLocation.makecommon(goal, Math.max(1, cost - 1), cost + 1);
		this.goal = goal;
	}

	@Override
	public Location getgoal() {
		return goal;
	}

	@Override
	public boolean validate(District d) {
		return super.validate(d) && d.getlocation(goal.getClass()) == null;
	}
}
