package javelin.model.world.location.town.labor;

import javelin.controller.Point;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;

public abstract class BuildingUpgrade extends Build {
	public BuildingUpgrade(int cost, Location previous) {
		super("Upgrade " + previous.toString().toLowerCase(), cost, previous);
	}

	@Override
	abstract public Location getgoal();

	@Override
	public void start() {
		previous.remove();
		super.start();
	}

	@Override
	public boolean validate(District d) {
		return site != null || d.getlocations().contains(previous);
	}

	@Override
	protected Point getsitelocation() {
		return previous.getlocation();
	}
}
